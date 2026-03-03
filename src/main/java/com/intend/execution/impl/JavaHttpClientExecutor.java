package com.intend.execution.impl;

import com.intend.core.MultipartPayload;
import com.intend.core.RequestIntent;
import com.intend.execution.ExecutionResult;
import com.intend.execution.RequestExecutor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.SSLException;

@Component
public class JavaHttpClientExecutor implements RequestExecutor {

    /** Connection-level timeout (TCP handshake). */
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);

    /** Per-request read timeout. */
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

    /** Responses larger than this threshold are streamed to a temp file. */
    private static final long STREAM_THRESHOLD_BYTES = 10 * 1024 * 1024; // 10 MB

    private final HttpClient client;

    public JavaHttpClientExecutor() {
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(CONNECT_TIMEOUT)
                .build();
    }

    // ── Main execution ─────────────────────────────────────────

    @Override
    public ExecutionResult execute(RequestIntent intent, Map<String, String> headers) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(intent.url())
                    .timeout(REQUEST_TIMEOUT);

            Map<String, String> requestHeaders = new HashMap<>(headers);
            HttpRequest.BodyPublisher bodyPublisher = resolveBodyPublisher(intent, builder, requestHeaders);

            requestHeaders.forEach((key, value) -> {
                if (!"Content-Type".equalsIgnoreCase(key)) {
                    builder.header(key, value);
                }
            });

            builder.method(intent.method().name(), bodyPublisher);

            System.out.println("Sending " + intent.method() + " to " + intent.url() + "...");

            long start = System.nanoTime();
            HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            long timeMs = (System.nanoTime() - start) / 1_000_000;

            // If the response is very large, warn but still return it
            String body = response.body();
            long sizeBytes = body != null
                    ? body.getBytes(java.nio.charset.StandardCharsets.UTF_8).length
                    : 0;

            if (sizeBytes > STREAM_THRESHOLD_BYTES) {
                System.out.println("⚠ Large response (" + (sizeBytes / (1024 * 1024)) + " MB) — consider streaming mode.");
            }

            return ExecutionResult.success(response.statusCode(), body, timeMs);

        } catch (HttpTimeoutException e) {
            return ExecutionResult.error("Request timed out after " + REQUEST_TIMEOUT.toSeconds() + "s.");

        } catch (ConnectException e) {
            return ExecutionResult.error("Connection refused — is the server running? (" + intent.url().getHost() + ")");

        } catch (UnknownHostException e) {
            return ExecutionResult.error("Unknown host: " + intent.url().getHost());

        } catch (SSLException e) {
            return ExecutionResult.error("SSL/TLS error: " + e.getMessage());

        } catch (IllegalArgumentException e) {
            return ExecutionResult.error("Invalid URL: " + e.getMessage());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ExecutionResult.error("Request interrupted.");

        } catch (Exception e) {
            return ExecutionResult.error("Network error: " + e.getMessage());
        }
    }

    // ── Streaming download ─────────────────────────────────────

    /**
     * Streams a large response directly to disk and returns the path.
     * Useful for file downloads or very large API responses.
     */
    public ExecutionResult executeStreaming(RequestIntent intent, Map<String, String> headers, Path destination) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(intent.url())
                    .timeout(Duration.ofMinutes(5));

            Map<String, String> requestHeaders = new HashMap<>(headers);
            requestHeaders.forEach((key, value) -> {
                if (!"Content-Type".equalsIgnoreCase(key)) {
                    builder.header(key, value);
                }
            });

            builder.method(intent.method().name(), HttpRequest.BodyPublishers.noBody());

            System.out.println("Streaming " + intent.method() + " to " + destination + "...");

            long start = System.nanoTime();
            HttpResponse<InputStream> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofInputStream());
            long sizeBytes;

            try (InputStream in = response.body()) {
                sizeBytes = Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
            }

            long timeMs = (System.nanoTime() - start) / 1_000_000;

            return new ExecutionResult(
                    response.statusCode(),
                    "Saved to " + destination + " (" + sizeBytes + " bytes)",
                    timeMs,
                    sizeBytes,
                    sizeBytes > 0 ? "Success" : "Empty"
            );

        } catch (HttpTimeoutException e) {
            return ExecutionResult.error("Streaming download timed out.");
        } catch (Exception e) {
            return ExecutionResult.error("Streaming error: " + e.getMessage());
        }
    }

    // ── Body resolution ────────────────────────────────────────

    private HttpRequest.BodyPublisher resolveBodyPublisher(
        RequestIntent intent,
        HttpRequest.Builder builder,
        Map<String, String> headers
    ) throws Exception {
        if (intent.payload() instanceof MultipartPayload mp) {
            MultipartUtil multipart = new MultipartUtil();
            multipart.addFilePart("file", mp.file().toPath());

            if (mp.hasBody()) {
                multipart.addFormField("data", mp.body());
            }

            headers.remove("Content-Type");
            builder.header("Content-Type", "multipart/form-data; boundary=" + multipart.getBoundary());
            System.out.println("Uploading File: " + mp.file().getName()
                    + (mp.hasBody() ? " + body fields" : ""));

            return multipart.build();
        }

        if (intent.payload() instanceof File file) {
            MultipartUtil multipart = new MultipartUtil();
            multipart.addFilePart("file", file.toPath());

            headers.remove("Content-Type");
            builder.header("Content-Type", "multipart/form-data; boundary=" + multipart.getBoundary());
            System.out.println("Uploading File: " + file.getName());

            return multipart.build();
        }

        return intent.payload() == null
            ? HttpRequest.BodyPublishers.noBody()
            : HttpRequest.BodyPublishers.ofString(intent.payload().toString());
    }
}
