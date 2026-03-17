package com.intend.execution.impl;

import com.intend.core.MultipartPayload;
import com.intend.core.RequestIntent;
import com.intend.execution.ExecutionResult;
import com.intend.execution.RequestExecutor;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import javax.net.ssl.SSLException;

@Component
public class JavaHttpClientExecutor implements RequestExecutor {

    /** Connection-level timeout (TCP handshake). */
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);

    /** Per-request read timeout. */
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

    /** Max automatic retries on transient failures (reuses same Idempotency-Key). */
    private static final int MAX_RETRIES = 2;

    /** Delay between retries. */
    private static final Duration RETRY_DELAY = Duration.ofSeconds(1);

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
        Map<String, String> requestHeaders = new HashMap<>(headers);
        Exception lastException = null;

        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            try {
                if (attempt > 0) {
                    System.out.println("⟳ Retry " + attempt + "/" + MAX_RETRIES
                            + " (same Idempotency-Key: " + requestHeaders.get("Idempotency-Key") + ")");
                    Thread.sleep(RETRY_DELAY.toMillis() * attempt);
                }

                HttpRequest.Builder builder = HttpRequest.newBuilder()
                        .uri(intent.url())
                        .timeout(REQUEST_TIMEOUT);

                HttpRequest.BodyPublisher bodyPublisher = resolveBodyPublisher(intent, builder, requestHeaders);

                requestHeaders.forEach((key, value) -> {
                    if (!"Content-Type".equalsIgnoreCase(key)) {
                        builder.header(key, value);
                    }
                });

                builder.method(intent.method().name(), bodyPublisher);

                System.out.println("Sending " + intent.method() + " to " + intent.url() + "...");

                long start = System.nanoTime();
                HttpResponse<InputStream> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofInputStream());
                long timeMs = (System.nanoTime() - start) / 1_000_000;

                String body = decodeResponseBody(response);
                long sizeBytes = body != null
                        ? body.getBytes(StandardCharsets.UTF_8).length
                        : 0;

                int status = response.statusCode();

                // Retry on 502/503/504 (server transient errors)
                if (isRetryableStatus(status) && attempt < MAX_RETRIES) {
                    System.out.println("⚠ Server returned " + status + " — will retry with same Idempotency-Key.");
                    continue;
                }

                if (sizeBytes > STREAM_THRESHOLD_BYTES) {
                    System.out.println("⚠ Large response (" + (sizeBytes / (1024 * 1024)) + " MB) — consider streaming mode.");
                }

                return ExecutionResult.success(status, body, timeMs, requestHeaders);

            } catch (HttpTimeoutException | ConnectException e) {
                lastException = e;
                // Transient — retry with same idempotency key
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

        // All retries exhausted
        if (lastException instanceof HttpTimeoutException) {
            return ExecutionResult.error("Request timed out after " + (MAX_RETRIES + 1)
                    + " attempts (" + REQUEST_TIMEOUT.toSeconds() + "s each).");
        }
        return ExecutionResult.error("Connection failed after " + (MAX_RETRIES + 1)
                + " attempts — is the server running? (" + intent.url().getHost() + ")");
    }

    private boolean isRetryableStatus(int status) {
        return status == 502 || status == 503 || status == 504;
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
                    sizeBytes > 0 ? "Success" : "Empty",
                    requestHeaders
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

    // ── Response decompression ─────────────────────────────────

    private String decodeResponseBody(HttpResponse<InputStream> response) throws Exception {
        String encoding = response.headers()
                .firstValue("Content-Encoding")
                .orElse("")
                .toLowerCase();

        try (InputStream raw = response.body();
             InputStream decoded = switch (encoding) {
                 case "gzip"    -> new GZIPInputStream(raw);
                 case "deflate" -> new InflaterInputStream(raw);
                 case "br"      -> brotliStream(raw);
                 default        -> raw;
             }) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            decoded.transferTo(out);
            return out.toString(StandardCharsets.UTF_8);
        }
    }

    private InputStream brotliStream(InputStream raw) throws Exception {
        try {
            Class<?> clazz = Class.forName("org.brotli.dec.BrotliInputStream");
            return (InputStream) clazz.getConstructor(InputStream.class).newInstance(raw);
        } catch (ClassNotFoundException e) {
            return raw;
        }
    }
}
