package com.intend.providers;

import com.intend.context.ResolutionContext;
import com.intend.core.RequestIntent;
import com.intend.spi.HeaderResolution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class IdempotencyProviderTest {

    private IdempotencyProvider provider;

    @BeforeEach
    void setUp() {
        provider = new IdempotencyProvider();
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
}
