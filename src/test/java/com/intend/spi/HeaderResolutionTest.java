package com.intend.spi;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HeaderResolutionTest {

    @Test
    void shouldCreateSuccessResolution() {
        Map<String, String> headers = Map.of("Authorization", "Bearer token123");
        HeaderResolution resolution = HeaderResolution.success(headers);

        assertTrue(resolution.success());
        assertEquals(headers, resolution.headers());
        assertNull(resolution.errorMessage());
    }

    @Test
    void shouldCreateFailureResolution() {
        HeaderResolution resolution = HeaderResolution.failure("Auth token expired");

        assertFalse(resolution.success());
        assertTrue(resolution.headers().isEmpty());
        assertEquals("Auth token expired", resolution.errorMessage());
    }

    @Test
    void shouldCreateSuccessWithMultipleHeaders() {
        Map<String, String> headers = Map.of(
            "Content-Type", "application/json",
            "Accept", "*/*",
            "X-Request-ID", "abc123"
        );
        HeaderResolution resolution = HeaderResolution.success(headers);

        assertTrue(resolution.success());
        assertEquals(3, resolution.headers().size());
        assertEquals("application/json", resolution.headers().get("Content-Type"));
    }

    @Test
    void shouldCreateSuccessWithEmptyHeaders() {
        HeaderResolution resolution = HeaderResolution.success(Map.of());
        assertTrue(resolution.success());
        assertTrue(resolution.headers().isEmpty());
    }
}
