package com.intend.providers;

import com.intend.context.ResolutionContext;
import com.intend.core.RequestIntent;
import com.intend.spi.HeaderResolution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ApiKeyProviderTest {

    private ApiKeyProvider provider;

    @BeforeEach
    void setUp() {
        provider = new ApiKeyProvider();
    }

    private ResolutionContext ctx(RequestIntent.AuthStrategy auth, Map<String, String> secrets) {
        RequestIntent intent = new RequestIntent(
            RequestIntent.Method.GET, URI.create("https://api.example.com"),
            null, auth, false, "dev"
        );
        return new ResolutionContext(intent, Map.of(), secrets);
    }

    @Test
    void shouldSupportApiKeyStrategy() {
        assertTrue(provider.supports(ctx(RequestIntent.AuthStrategy.API_KEY, Map.of())));
    }

    @Test
    void shouldNotSupportOtherStrategies() {
        assertFalse(provider.supports(ctx(RequestIntent.AuthStrategy.NONE, Map.of())));
        assertFalse(provider.supports(ctx(RequestIntent.AuthStrategy.BEARER_TOKEN, Map.of())));
        assertFalse(provider.supports(ctx(RequestIntent.AuthStrategy.BASIC_AUTH, Map.of())));
    }

    @Test
    void shouldResolveApiKeyFromSecrets() {
        HeaderResolution res = provider.resolve(ctx(RequestIntent.AuthStrategy.API_KEY, Map.of("API_KEY", "my-secret-key")));
        assertTrue(res.success());
        assertEquals("my-secret-key", res.headers().get("X-API-KEY"));
    }

    @Test
    void shouldUseFallbackWhenKeyMissing() {
        HeaderResolution res = provider.resolve(ctx(RequestIntent.AuthStrategy.API_KEY, Map.of()));
        assertTrue(res.success());
        assertEquals("MISSING_KEY", res.headers().get("X-API-KEY"));
    }
}
