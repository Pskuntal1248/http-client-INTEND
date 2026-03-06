package com.intend.providers;

import com.intend.context.ResolutionContext;
import com.intend.core.RequestIntent;
import com.intend.spi.HeaderResolution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BasicAuthProviderTest {

    private BasicAuthProvider provider;

    @BeforeEach
    void setUp() {
        provider = new BasicAuthProvider();
    }

    private ResolutionContext ctx(RequestIntent.AuthStrategy auth, Map<String, String> secrets) {
        RequestIntent intent = new RequestIntent(
            RequestIntent.Method.GET, URI.create("https://api.example.com"),
            null, auth, false, "dev"
        );
        return new ResolutionContext(intent, Map.of(), secrets);
    }

    @Test
    void shouldSupportBasicAuthStrategy() {
        assertTrue(provider.supports(ctx(RequestIntent.AuthStrategy.BASIC_AUTH, Map.of())));
    }

    @Test
    void shouldNotSupportOtherStrategies() {
        assertFalse(provider.supports(ctx(RequestIntent.AuthStrategy.NONE, Map.of())));
        assertFalse(provider.supports(ctx(RequestIntent.AuthStrategy.API_KEY, Map.of())));
    }

    @Test
    void shouldResolveBasicAuthFromSecrets() {
        HeaderResolution res = provider.resolve(ctx(RequestIntent.AuthStrategy.BASIC_AUTH,
            Map.of("BASIC_USER", "myuser", "BASIC_PASS", "mypass")));
        assertTrue(res.success());
        String expected = "Basic " + Base64.getEncoder().encodeToString("myuser:mypass".getBytes(StandardCharsets.UTF_8));
        assertEquals(expected, res.headers().get("Authorization"));
    }

    @Test
    void shouldUseFallbackCredentials() {
        HeaderResolution res = provider.resolve(ctx(RequestIntent.AuthStrategy.BASIC_AUTH, Map.of()));
        assertTrue(res.success());
        String expected = "Basic " + Base64.getEncoder().encodeToString("admin:password".getBytes(StandardCharsets.UTF_8));
        assertEquals(expected, res.headers().get("Authorization"));
    }
}
