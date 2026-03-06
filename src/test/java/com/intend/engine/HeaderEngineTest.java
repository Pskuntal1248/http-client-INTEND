package com.intend.engine;

import com.intend.context.ResolutionContext;
import com.intend.core.RequestIntent;
import com.intend.spi.HeaderProvider;
import com.intend.spi.HeaderResolution;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HeaderEngineTest {

    private ResolutionContext createContext(RequestIntent.AuthStrategy auth) {
        RequestIntent intent = new RequestIntent(
            RequestIntent.Method.GET,
            URI.create("https://api.example.com"),
            null,
            auth,
            false,
            "dev"
        );
        return new ResolutionContext(intent, Map.of(), Map.of());
    }

    @Test
    void shouldReturnEmptyHeadersWhenNoProviders() {
        HeaderEngine engine = new HeaderEngine(List.of());
        Map<String, String> headers = engine.execute(createContext(RequestIntent.AuthStrategy.NONE));
        assertTrue(headers.isEmpty());
    }

    @Test
    void shouldCollectHeadersFromSupportedProviders() {
        HeaderProvider provider = new HeaderProvider() {
            public int getOrder() { return 1; }
            public boolean supports(ResolutionContext ctx) { return true; }
            public HeaderResolution resolve(ResolutionContext ctx) {
                return HeaderResolution.success(Map.of("Accept", "application/json"));
            }
        };

        HeaderEngine engine = new HeaderEngine(List.of(provider));
        Map<String, String> headers = engine.execute(createContext(RequestIntent.AuthStrategy.NONE));
        assertEquals("application/json", headers.get("Accept"));
    }

    @Test
    void shouldSkipUnsupportedProviders() {
        HeaderProvider supported = new HeaderProvider() {
            public int getOrder() { return 1; }
            public boolean supports(ResolutionContext ctx) { return true; }
            public HeaderResolution resolve(ResolutionContext ctx) {
                return HeaderResolution.success(Map.of("X-Supported", "yes"));
            }
        };

        HeaderProvider unsupported = new HeaderProvider() {
            public int getOrder() { return 2; }
            public boolean supports(ResolutionContext ctx) { return false; }
            public HeaderResolution resolve(ResolutionContext ctx) {
                return HeaderResolution.success(Map.of("X-Unsupported", "no"));
            }
        };

        HeaderEngine engine = new HeaderEngine(List.of(supported, unsupported));
        Map<String, String> headers = engine.execute(createContext(RequestIntent.AuthStrategy.NONE));
        assertEquals("yes", headers.get("X-Supported"));
        assertNull(headers.get("X-Unsupported"));
    }

    @Test
    void shouldMergeHeadersFromMultipleProviders() {
        HeaderProvider first = new HeaderProvider() {
            public int getOrder() { return 1; }
            public boolean supports(ResolutionContext ctx) { return true; }
            public HeaderResolution resolve(ResolutionContext ctx) {
                return HeaderResolution.success(Map.of("Accept", "*/*"));
            }
        };

        HeaderProvider second = new HeaderProvider() {
            public int getOrder() { return 2; }
            public boolean supports(ResolutionContext ctx) { return true; }
            public HeaderResolution resolve(ResolutionContext ctx) {
                return HeaderResolution.success(Map.of("X-Custom", "value"));
            }
        };

        HeaderEngine engine = new HeaderEngine(List.of(second, first));
        Map<String, String> headers = engine.execute(createContext(RequestIntent.AuthStrategy.NONE));
        assertEquals("*/*", headers.get("Accept"));
        assertEquals("value", headers.get("X-Custom"));
    }

    @Test
    void shouldHandleFailedProviderGracefully() {
        HeaderProvider failing = new HeaderProvider() {
            public int getOrder() { return 1; }
            public boolean supports(ResolutionContext ctx) { return true; }
            public HeaderResolution resolve(ResolutionContext ctx) {
                return HeaderResolution.failure("Token expired");
            }
        };

        HeaderProvider working = new HeaderProvider() {
            public int getOrder() { return 2; }
            public boolean supports(ResolutionContext ctx) { return true; }
            public HeaderResolution resolve(ResolutionContext ctx) {
                return HeaderResolution.success(Map.of("Accept", "*/*"));
            }
        };

        HeaderEngine engine = new HeaderEngine(List.of(failing, working));
        Map<String, String> headers = engine.execute(createContext(RequestIntent.AuthStrategy.NONE));
        assertEquals("*/*", headers.get("Accept"));
        assertEquals(1, headers.size());
    }

    @Test
    void shouldExecuteProvidersInOrder() {
        HeaderProvider low = new HeaderProvider() {
            public int getOrder() { return 10; }
            public boolean supports(ResolutionContext ctx) { return true; }
            public HeaderResolution resolve(ResolutionContext ctx) {
                return HeaderResolution.success(Map.of("X-Header", "from-low"));
            }
        };

        HeaderProvider high = new HeaderProvider() {
            public int getOrder() { return 99; }
            public boolean supports(ResolutionContext ctx) { return true; }
            public HeaderResolution resolve(ResolutionContext ctx) {
                return HeaderResolution.success(Map.of("X-Header", "from-high"));
            }
        };

        HeaderEngine engine = new HeaderEngine(List.of(high, low));
        Map<String, String> headers = engine.execute(createContext(RequestIntent.AuthStrategy.NONE));
        // Higher order runs later and overwrites
        assertEquals("from-high", headers.get("X-Header"));
    }
}
