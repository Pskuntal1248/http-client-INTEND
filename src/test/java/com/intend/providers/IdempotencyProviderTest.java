package com.intend.providers;

import com.intend.context.ResolutionContext;
import com.intend.core.RequestIntent;
import com.intend.repository.StateRepository;
import com.intend.spi.HeaderResolution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class IdempotencyProviderTest {

    private IdempotencyProvider provider;
    private TestStateRepository stateRepo;

    static class TestStateRepository implements StateRepository {
        private final Map<String, String> keys = new HashMap<>();
        public void saveIdempotencyKey(String fingerprint, String key) { keys.put(fingerprint, key); }
        public String getLastIdempotencyKey(String fingerprint) { return keys.get(fingerprint); }
    }

    @BeforeEach
    void setUp() {
        stateRepo = new TestStateRepository();
        provider = new IdempotencyProvider(stateRepo);
    }

    private ResolutionContext ctx(RequestIntent.Method method, boolean forceNew) {
        RequestIntent intent = new RequestIntent(
            method, URI.create("https://api.example.com/orders"),
            "{}", RequestIntent.AuthStrategy.NONE, forceNew, "dev"
        );
        return new ResolutionContext(intent, Map.of(), Map.of());
    }

    @Test
    void shouldHaveOrder50() {
        assertEquals(50, provider.getOrder());
    }

    @Test
    void shouldSupportPostPutPatch() {
        assertTrue(provider.supports(ctx(RequestIntent.Method.POST, false)));
        assertTrue(provider.supports(ctx(RequestIntent.Method.PUT, false)));
        assertTrue(provider.supports(ctx(RequestIntent.Method.PATCH, false)));
    }

    @Test
    void shouldNotSupportGetDelete() {
        assertFalse(provider.supports(ctx(RequestIntent.Method.GET, false)));
        assertFalse(provider.supports(ctx(RequestIntent.Method.DELETE, false)));
    }

    @Test
    void shouldGenerateNewKeyOnFirstRequest() {
        HeaderResolution res = provider.resolve(ctx(RequestIntent.Method.POST, false));
        assertTrue(res.success());
        assertNotNull(res.headers().get("Idempotency-Key"));
        assertNotNull(res.headers().get("X-Request-ID"));
        assertEquals(res.headers().get("Idempotency-Key"), res.headers().get("X-Request-ID"));
    }

    @Test
    void shouldReuseExistingKey() {
        HeaderResolution first = provider.resolve(ctx(RequestIntent.Method.POST, false));
        String firstKey = first.headers().get("Idempotency-Key");

        HeaderResolution second = provider.resolve(ctx(RequestIntent.Method.POST, false));
        String secondKey = second.headers().get("Idempotency-Key");

        assertEquals(firstKey, secondKey);
    }

    @Test
    void shouldForceNewKeyWhenRequested() {
        HeaderResolution first = provider.resolve(ctx(RequestIntent.Method.POST, false));
        String firstKey = first.headers().get("Idempotency-Key");

        HeaderResolution forced = provider.resolve(ctx(RequestIntent.Method.POST, true));
        String forcedKey = forced.headers().get("Idempotency-Key");

        assertNotEquals(firstKey, forcedKey);
    }

    @Test
    void shouldGenerateDifferentKeysForDifferentEndpoints() {
        RequestIntent intent1 = new RequestIntent(RequestIntent.Method.POST, URI.create("https://api.com/a"), "{}", RequestIntent.AuthStrategy.NONE, false, "dev");
        RequestIntent intent2 = new RequestIntent(RequestIntent.Method.POST, URI.create("https://api.com/b"), "{}", RequestIntent.AuthStrategy.NONE, false, "dev");

        HeaderResolution res1 = provider.resolve(new ResolutionContext(intent1, Map.of(), Map.of()));
        HeaderResolution res2 = provider.resolve(new ResolutionContext(intent2, Map.of(), Map.of()));

        assertNotEquals(res1.headers().get("Idempotency-Key"), res2.headers().get("Idempotency-Key"));
    }
}
