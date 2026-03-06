package com.intend.providers;

import com.intend.context.ResolutionContext;
import com.intend.core.RequestIntent;
import com.intend.spi.HeaderResolution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BearerTokenProviderTest {

    private BearerTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = new BearerTokenProvider();
    }

    private ResolutionContext ctx(RequestIntent.AuthStrategy auth, Map<String, String> secrets) {
        RequestIntent intent = new RequestIntent(
            RequestIntent.Method.GET, URI.create("https://api.example.com"),
            null, auth, false, "dev"
        );
        return new ResolutionContext(intent, Map.of(), secrets);
    }

    @Test
    void shouldSupportBearerTokenStrategy() {
        assertTrue(provider.supports(ctx(RequestIntent.AuthStrategy.BEARER_TOKEN, Map.of())));
    }

    @Test
    void shouldNotSupportOtherStrategies() {
        assertFalse(provider.supports(ctx(RequestIntent.AuthStrategy.NONE, Map.of())));
        assertFalse(provider.supports(ctx(RequestIntent.AuthStrategy.API_KEY, Map.of())));
    }

    @Test
    void shouldResolveTokenFromSecrets() {
        HeaderResolution res = provider.resolve(ctx(RequestIntent.AuthStrategy.BEARER_TOKEN,
            Map.of("ACCESS_TOKEN", "real-jwt-token")));
        assertTrue(res.success());
        assertEquals("Bearer real-jwt-token", res.headers().get("Authorization"));
    }

    @Test
    void shouldUseMockTokenWhenMissing() {
        HeaderResolution res = provider.resolve(ctx(RequestIntent.AuthStrategy.BEARER_TOKEN, Map.of()));
        assertTrue(res.success());
        assertTrue(res.headers().get("Authorization").startsWith("Bearer ey"));
    }
}
