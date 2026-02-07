package com.intend.execution.impl;

import com.intend.core.RequestIntent;
import com.intend.execution.RequestExecutor;
import org.springframework.stereotype.Component;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Component
public class JavaHttpClientExecutor implements RequestExecutor {

    private final HttpClient client;

    public JavaHttpClientExecutor() {
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public String execute(RequestIntent intent, Map<String, String> headers) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(intent.url());

            headers.forEach(builder::header);

            HttpRequest.BodyPublisher bodyPublisher = (intent.payload() == null)
                    ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofString(intent.payload().toString());

            builder.method(intent.method().name(), bodyPublisher);

            System.out.println("🚀 Sending " + intent.method() + " to " + intent.url() + "...");
            HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());

            return String.format("Status: %d\nBody: %s", response.statusCode(), response.body());

        } catch (Exception e) {
            throw new RuntimeException("Network Error: " + e.getMessage(), e);
        }
    }
}
