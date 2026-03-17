package com.intend.execution;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

/**
 * Rich result returned by {@link RequestExecutor#execute}.
 * Carries status code, body, timing, size, a human-readable status category,
 * and the resolved request headers that were actually sent.
 */
public record ExecutionResult(
        int statusCode,
        String body,
        long timeMs,
        long sizeBytes,
        String statusCategory,
        Map<String, String> requestHeaders
) {

    public static ExecutionResult success(int statusCode, String body, long timeMs) {
        long size = body != null ? body.getBytes(StandardCharsets.UTF_8).length : 0;
        return new ExecutionResult(statusCode, body, timeMs, size, categorise(statusCode), Collections.emptyMap());
    }

    public static ExecutionResult success(int statusCode, String body, long timeMs, Map<String, String> requestHeaders) {
        long size = body != null ? body.getBytes(StandardCharsets.UTF_8).length : 0;
        return new ExecutionResult(statusCode, body, timeMs, size, categorise(statusCode),
                requestHeaders != null ? requestHeaders : Collections.emptyMap());
    }

    public static ExecutionResult error(String message) {
        return new ExecutionResult(0, message, 0, 0, "Error", Collections.emptyMap());
    }



    public boolean isSuccess() {
        return statusCode >= 200 && statusCode < 300;
    }

    public boolean isClientError() {
        return statusCode >= 400 && statusCode < 500;
    }

    public boolean isServerError() {
        return statusCode >= 500;
    }

    /**
     * Returns the legacy {@code "Status: …\nBody: …"} string for backward
     * compatibility with callers that still parse the raw text.
     */
    public String toRawString() {
        return String.format("Status: %d\nBody: %s", statusCode, body);
    }

    /**
     * Returns a formatted summary suitable for CLI output.
     */
    public String toPrettyString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Status: ").append(statusCode).append(" (").append(statusCategory).append(")\n");
        sb.append("Time:   ").append(timeMs).append(" ms\n");
        sb.append("Size:   ").append(formatSize(sizeBytes)).append("\n");
        sb.append("Body:\n").append(body != null ? body : "");
        return sb.toString();
    }

   
    private static String categorise(int code) {
        if (code >= 200 && code < 300) return "Success";
        if (code >= 300 && code < 400) return "Redirect";
        if (code >= 400 && code < 500) return "Client Error";
        if (code >= 500)               return "Server Error";
        return "Unknown";
    }

    private static String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024));
    }
}
