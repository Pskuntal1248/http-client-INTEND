package com.intend.providers;

import com.intend.context.ResolutionContext;
import com.intend.core.RequestIntent;
import com.intend.spi.HeaderResolution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProtocolProviderTest {

    private ProtocolProvider provider;

    @BeforeEach
    void setUp() {
        provider = new ProtocolProvider();
    }

    private ResolutionContext ctx(Object payload) {
        RequestIntent intent = new RequestIntent(
            RequestIntent.Method.POST,
            URI.create("https://api.example.com"),
            payload,
            RequestIntent.AuthStrategy.NONE,
            false,
            "dev"
        );
        return new ResolutionContext(intent, Map.of(), Map.of());
    }

    @Test
    void shouldHaveOrder10() {
        assertEquals(10, provider.getOrder());
    }

    @Test
    void shouldAlwaysSupport() {
        assertTrue(provider.supports(ctx(null)));
        assertTrue(provider.supports(ctx("{}")));
    }

    @Test
    void shouldSetJsonContentType() {
        HeaderResolution res = provider.resolve(ctx("{\"name\": \"test\"}"));
        assertTrue(res.success());
        assertEquals("application/json", res.headers().get("Content-Type"));
        assertEquals("*/*", res.headers().get("Accept"));
    }

    @Test
    void shouldSetJsonContentTypeForArray() {
        HeaderResolution res = provider.resolve(ctx("[1, 2, 3]"));
        assertTrue(res.success());
        assertEquals("application/json", res.headers().get("Content-Type"));
    }

    @Test
    void shouldSetXmlContentType() {
        HeaderResolution res = provider.resolve(ctx("<root><item/></root>"));
        assertTrue(res.success());
        assertEquals("application/xml", res.headers().get("Content-Type"));
    }

    @Test
    void shouldSetTextPlainContentType() {
        HeaderResolution res = provider.resolve(ctx("hello world"));
        assertTrue(res.success());
        assertEquals("text/plain", res.headers().get("Content-Type"));
    }

    @Test
    void shouldOnlySetAcceptWhenNoPayload() {
        HeaderResolution res = provider.resolve(ctx(null));
        assertTrue(res.success());
        assertEquals("*/*", res.headers().get("Accept"));
        assertNull(res.headers().get("Content-Type"));
    }

    @Test
    void shouldOnlySetAcceptWhenEmptyPayload() {
        HeaderResolution res = provider.resolve(ctx(""));
        assertTrue(res.success());
        assertEquals("*/*", res.headers().get("Accept"));
        assertNull(res.headers().get("Content-Type"));
    }
}
