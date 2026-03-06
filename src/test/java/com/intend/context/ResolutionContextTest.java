package com.intend.context;

import com.intend.core.RequestIntent;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ResolutionContextTest {

    @Test
    void shouldCreateContext() {
        RequestIntent intent = new RequestIntent(
            RequestIntent.Method.GET, URI.create("http://localhost"),
            null, RequestIntent.AuthStrategy.NONE, false, "dev"
        );
        Map<String, String> config = Map.of("BASE_URL", "http://localhost");
        Map<String, String> secrets = Map.of("API_KEY", "secret");

        ResolutionContext ctx = new ResolutionContext(intent, config, secrets);

        assertEquals(intent, ctx.intent());
        assertEquals("http://localhost", ctx.config().get("BASE_URL"));
        assertEquals("secret", ctx.secrets().get("API_KEY"));
    }

    @Test
    void shouldSupportRecordEquality() {
        RequestIntent intent = new RequestIntent(
            RequestIntent.Method.GET, URI.create("http://localhost"),
            null, RequestIntent.AuthStrategy.NONE, false, "dev"
        );
        ResolutionContext a = new ResolutionContext(intent, Map.of(), Map.of());
        ResolutionContext b = new ResolutionContext(intent, Map.of(), Map.of());
        assertEquals(a, b);
    }
}
