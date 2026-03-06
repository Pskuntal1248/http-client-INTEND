package com.intend.integration;

import com.intend.config.EngineConfig;
import com.intend.context.ResolutionContext;
import com.intend.core.RequestIntent;
import com.intend.engine.HeaderEngine;
import com.intend.engine.TemplateEngine;
import com.intend.providers.*;
import com.intend.repository.StateRepository;
import com.intend.repository.VariableRepository;
import com.intend.spi.HeaderProvider;
import org.junit.jupiter.api.*;

import java.net.URI;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("End-to-End Integration Tests – Full Pipeline Without Network")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EndToEndIntegrationTest {

    // Shared components (real implementations, no mocks)
    private static VariableRepository variableRepository;
    private static TemplateEngine templateEngine;
    private static HeaderEngine headerEngine;
    private static InMemoryStateRepository stateRepository;

    @BeforeAll
    static void initPipeline() {
        variableRepository = new VariableRepository();
        templateEngine = new TemplateEngine(variableRepository);
        stateRepository = new InMemoryStateRepository();

        List<HeaderProvider> providers = List.of(
            new ProtocolProvider(),
            new IdempotencyProvider(stateRepository),
            new ApiKeyProvider(),
            new BasicAuthProvider(),
            new BearerTokenProvider()
        );
        headerEngine = new HeaderEngine(providers);
    }

    // ── Scenario 1: Simple GET Request ─────────────────────────────

    @Test
    @Order(1)
    @DisplayName("E2E: Simple GET request produces Accept header only")
    void simpleGetRequest() {
        RequestIntent intent = new RequestIntent(
            RequestIntent.Method.GET,
            URI.create("https://api.example.com/users"),
            null,
            RequestIntent.AuthStrategy.NONE,
            false, "dev"
        );

        ResolutionContext ctx = new ResolutionContext(intent, Map.of(), Map.of());
        Map<String, String> headers = headerEngine.execute(ctx);

        assertThat(headers)
            .containsEntry("Accept", "*/*")
            .doesNotContainKey("Content-Type")
            .doesNotContainKey("Authorization")
            .doesNotContainKey("Idempotency-Key");
    }

    // ── Scenario 2: POST with JSON + Bearer Auth ───────────────────

    @Test
    @Order(2)
    @DisplayName("E2E: POST /orders with Bearer auth and JSON payload")
    void postWithBearerAuth() {
        String body = "{\"item\": \"Widget\", \"qty\": 5}";
        RequestIntent intent = new RequestIntent(
            RequestIntent.Method.POST,
            URI.create("https://api.example.com/orders"),
            body,
            RequestIntent.AuthStrategy.BEARER_TOKEN,
            false, "dev"
        );

        Map<String, String> secrets = Map.of("ACCESS_TOKEN", "real-jwt-token-123");
        ResolutionContext ctx = new ResolutionContext(intent, Map.of(), secrets);
        Map<String, String> headers = headerEngine.execute(ctx);

        assertThat(headers)
            .containsEntry("Accept", "*/*")
            .containsEntry("Content-Type", "application/json")
            .containsEntry("Authorization", "Bearer real-jwt-token-123")
            .containsKey("Idempotency-Key")
            .containsKey("X-Request-ID");
    }

    // ── Scenario 3: POST with API Key Auth ─────────────────────────

    @Test
    @Order(3)
    @DisplayName("E2E: POST /data with API Key auth")
    void postWithApiKey() {
        RequestIntent intent = new RequestIntent(
            RequestIntent.Method.POST,
            URI.create("https://api.example.com/data"),
            "{\"key\": \"value\"}",
            RequestIntent.AuthStrategy.API_KEY,
            false, "dev"
        );

        Map<String, String> secrets = Map.of("API_KEY", "sk-12345");
        ResolutionContext ctx = new ResolutionContext(intent, Map.of(), secrets);
        Map<String, String> headers = headerEngine.execute(ctx);

        assertThat(headers)
            .containsEntry("X-API-KEY", "sk-12345")
            .containsEntry("Content-Type", "application/json")
            .containsKey("Idempotency-Key");
    }

    // ── Scenario 4: PUT with Basic Auth ────────────────────────────

    @Test
    @Order(4)
    @DisplayName("E2E: PUT with Basic Auth generates correct headers")
    void putWithBasicAuth() {
        RequestIntent intent = new RequestIntent(
            RequestIntent.Method.PUT,
            URI.create("https://api.example.com/users/1"),
            "{\"name\": \"Updated\"}",
            RequestIntent.AuthStrategy.BASIC_AUTH,
            false, "dev"
        );

        Map<String, String> secrets = Map.of("BASIC_USER", "admin", "BASIC_PASS", "s3cret");
        ResolutionContext ctx = new ResolutionContext(intent, Map.of(), secrets);
        Map<String, String> headers = headerEngine.execute(ctx);

        assertThat(headers)
            .containsEntry("Content-Type", "application/json")
            .containsKey("Authorization")
            .containsKey("Idempotency-Key");

        assertThat(headers.get("Authorization")).startsWith("Basic ");
    }

    // ── Scenario 5: Template Engine full resolution ────────────────

    @Test
    @Order(5)
    @DisplayName("E2E: Template engine resolves URL and body variables")
    void templateResolution() {
        variableRepository.put("baseUrl", "https://api.example.com");
        variableRepository.put("version", "v2");
        variableRepository.put("userName", "Alice");

        String urlTemplate = "{{baseUrl}}/{{version}}/users";
        String bodyTemplate = "{\"name\": \"{{userName}}\", \"id\": \"{{uuid}}\"}";

        String resolvedUrl = templateEngine.process(urlTemplate);
        String resolvedBody = templateEngine.process(bodyTemplate);

        assertThat(resolvedUrl).isEqualTo("https://api.example.com/v2/users");
        assertThat(resolvedBody).contains("\"name\": \"Alice\"");
        assertThat(resolvedBody).containsPattern("\"id\": \"[a-f0-9-]+\"");
    }

    // ── Scenario 6: Idempotency Key Reuse ──────────────────────────

    @Test
    @Order(6)
    @DisplayName("E2E: Idempotency key is reused on retry, fresh on forceNew")
    void idempotencyKeyLifecycle() {
        URI orderUrl = URI.create("https://api.example.com/orders");

        // First POST – generates a key
        RequestIntent first = new RequestIntent(
            RequestIntent.Method.POST, orderUrl, "{}",
            RequestIntent.AuthStrategy.NONE, false, "dev"
        );
        ResolutionContext ctx1 = new ResolutionContext(first, Map.of(), Map.of());
        Map<String, String> h1 = headerEngine.execute(ctx1);
        String firstKey = h1.get("Idempotency-Key");
        assertThat(firstKey).isNotNull();

        // Retry – same key reused
        Map<String, String> h2 = headerEngine.execute(ctx1);
        assertThat(h2.get("Idempotency-Key")).isEqualTo(firstKey);

        // Force new – different key
        RequestIntent forceNew = new RequestIntent(
            RequestIntent.Method.POST, orderUrl, "{}",
            RequestIntent.AuthStrategy.NONE, true, "dev"
        );
        ResolutionContext ctx2 = new ResolutionContext(forceNew, Map.of(), Map.of());
        Map<String, String> h3 = headerEngine.execute(ctx2);
        assertThat(h3.get("Idempotency-Key")).isNotEqualTo(firstKey);
    }

    // ── Scenario 7: DELETE should not get idempotency key ──────────

    @Test
    @Order(7)
    @DisplayName("E2E: DELETE request should NOT have Idempotency-Key")
    void deleteNoIdempotency() {
        RequestIntent intent = new RequestIntent(
            RequestIntent.Method.DELETE,
            URI.create("https://api.example.com/users/99"),
            null,
            RequestIntent.AuthStrategy.BEARER_TOKEN,
            false, "dev"
        );

        Map<String, String> secrets = Map.of("ACCESS_TOKEN", "delete-token");
        ResolutionContext ctx = new ResolutionContext(intent, Map.of(), secrets);
        Map<String, String> headers = headerEngine.execute(ctx);

        assertThat(headers)
            .containsEntry("Authorization", "Bearer delete-token")
            .doesNotContainKey("Idempotency-Key")
            .doesNotContainKey("X-Request-ID");
    }

    // ── Scenario 8: XML payload detection ──────────────────────────

    @Test
    @Order(8)
    @DisplayName("E2E: XML payload sets Content-Type to application/xml")
    void xmlPayload() {
        RequestIntent intent = new RequestIntent(
            RequestIntent.Method.POST,
            URI.create("https://api.example.com/xml-endpoint"),
            "<root><item>test</item></root>",
            RequestIntent.AuthStrategy.NONE,
            false, "dev"
        );

        ResolutionContext ctx = new ResolutionContext(intent, Map.of(), Map.of());
        Map<String, String> headers = headerEngine.execute(ctx);

        assertThat(headers).containsEntry("Content-Type", "application/xml");
    }

    // ── Scenario 9: Plain text payload ─────────────────────────────

    @Test
    @Order(9)
    @DisplayName("E2E: Plain text payload sets Content-Type to text/plain")
    void plainTextPayload() {
        RequestIntent intent = new RequestIntent(
            RequestIntent.Method.PATCH,
            URI.create("https://api.example.com/notes/1"),
            "This is just plain text",
            RequestIntent.AuthStrategy.NONE,
            false, "dev"
        );

        ResolutionContext ctx = new ResolutionContext(intent, Map.of(), Map.of());
        Map<String, String> headers = headerEngine.execute(ctx);

        assertThat(headers).containsEntry("Content-Type", "text/plain");
    }

    // ── Scenario 10: Full pipeline variable capture simulation ─────

    @Test
    @Order(10)
    @DisplayName("E2E: Variable stored via repo is available in template engine")
    void variableCaptureAndReuse() {
        // Simulate capturing a variable from a response
        variableRepository.put("orderId", "ORD-9001");

        // Now use it in a template
        String url = templateEngine.process("https://api.example.com/orders/{{orderId}}");
        assertThat(url).isEqualTo("https://api.example.com/orders/ORD-9001");

        // Use it in a body
        String body = templateEngine.process("{\"orderId\": \"{{orderId}}\", \"status\": \"shipped\"}");
        assertThat(body).isEqualTo("{\"orderId\": \"ORD-9001\", \"status\": \"shipped\"}");
    }

    // ── Helper ─────────────────────────────────────────────────────

    static class InMemoryStateRepository implements StateRepository {
        private final Map<String, String> store = new HashMap<>();

        @Override
        public String getLastIdempotencyKey(String key) {
            return store.get(key);
        }

        @Override
        public void saveIdempotencyKey(String key, String uuid) {
            store.put(key, uuid);
        }
    }
}
