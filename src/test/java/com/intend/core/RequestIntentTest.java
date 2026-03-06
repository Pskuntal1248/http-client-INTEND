package com.intend.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class RequestIntentTest {

    @Test
    void shouldCreateRequestIntent() {
        RequestIntent intent = new RequestIntent(
            RequestIntent.Method.GET,
            URI.create("https://api.example.com/users"),
            null,
            RequestIntent.AuthStrategy.NONE,
            false,
            "dev"
        );

        assertEquals(RequestIntent.Method.GET, intent.method());
        assertEquals(URI.create("https://api.example.com/users"), intent.url());
        assertNull(intent.payload());
        assertEquals(RequestIntent.AuthStrategy.NONE, intent.auth());
        assertFalse(intent.forceNew());
        assertEquals("dev", intent.env());
    }

    @Test
    void shouldCreateRequestIntentWithPayload() {
        String body = "{\"name\": \"John\"}";
        RequestIntent intent = new RequestIntent(
            RequestIntent.Method.POST,
            URI.create("https://api.example.com/users"),
            body,
            RequestIntent.AuthStrategy.BEARER_TOKEN,
            true,
            "prod"
        );

        assertEquals(RequestIntent.Method.POST, intent.method());
        assertEquals(body, intent.payload());
        assertEquals(RequestIntent.AuthStrategy.BEARER_TOKEN, intent.auth());
        assertTrue(intent.forceNew());
        assertEquals("prod", intent.env());
    }

    @ParameterizedTest
    @EnumSource(RequestIntent.Method.class)
    void shouldHaveAllHttpMethods(RequestIntent.Method method) {
        assertNotNull(method.name());
    }

    @Test
    void shouldHaveCorrectHttpMethods() {
        RequestIntent.Method[] methods = RequestIntent.Method.values();
        assertEquals(5, methods.length);
        assertArrayEquals(
            new RequestIntent.Method[]{
                RequestIntent.Method.GET,
                RequestIntent.Method.POST,
                RequestIntent.Method.PUT,
                RequestIntent.Method.DELETE,
                RequestIntent.Method.PATCH
            },
            methods
        );
    }

    @ParameterizedTest
    @EnumSource(RequestIntent.AuthStrategy.class)
    void shouldHaveAllAuthStrategies(RequestIntent.AuthStrategy strategy) {
        assertNotNull(strategy.name());
    }

    @Test
    void shouldHaveCorrectAuthStrategies() {
        RequestIntent.AuthStrategy[] strategies = RequestIntent.AuthStrategy.values();
        assertEquals(4, strategies.length);
    }

    @Test
    void shouldSupportRecordEquality() {
        RequestIntent a = new RequestIntent(RequestIntent.Method.GET, URI.create("http://a.com"), null, RequestIntent.AuthStrategy.NONE, false, "dev");
        RequestIntent b = new RequestIntent(RequestIntent.Method.GET, URI.create("http://a.com"), null, RequestIntent.AuthStrategy.NONE, false, "dev");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void shouldNotBeEqualWithDifferentFields() {
        RequestIntent a = new RequestIntent(RequestIntent.Method.GET, URI.create("http://a.com"), null, RequestIntent.AuthStrategy.NONE, false, "dev");
        RequestIntent b = new RequestIntent(RequestIntent.Method.POST, URI.create("http://a.com"), null, RequestIntent.AuthStrategy.NONE, false, "dev");
        assertNotEquals(a, b);
    }
}
