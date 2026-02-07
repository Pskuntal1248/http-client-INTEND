package com.intend.spi;

import java.util.Map;

public record HeaderResolution(
    boolean success,
    Map<String, String> headers,
    String errorMessage
) {
    public static HeaderResolution success(Map<String, String> headers) {
        return new HeaderResolution(true, headers, null);
    }

    public static HeaderResolution failure(String message) {
        return new HeaderResolution(false, Map.of(), message);
    }
}
