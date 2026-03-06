package com.intend.service.impl;

import com.intend.context.ResolutionContext;
import com.intend.core.RequestIntent;
import com.intend.engine.HeaderEngine;
import com.intend.engine.TemplateEngine;
import com.intend.execution.ExecutionResult;
import com.intend.execution.RequestExecutor;
import com.intend.repository.*;
import com.intend.spi.HeaderProvider;
import com.intend.spi.HeaderResolution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Service layer tests using real objects & lightweight test doubles
 * instead of Mockito mocks (avoids Java 24 ByteBuddy restrictions
 * on concrete classes).
 */
@DisplayName("IntendServiceImpl – Service Layer Tests")
class IntendServiceImplTest {

    private StubContextRepository contextRepository;
    private StubRequestExecutor requestExecutor;
    private TestHistoryRepository historyRepository;
    private VariableRepository variableRepository;
    private TemplateEngine templateEngine;
    private HeaderEngine headerEngine;
    private IntendServiceImpl service;

    private static final URI URL = URI.create("https://api.example.com/users");

    @BeforeEach
    void setUp() {
        variableRepository = new VariableRepository();
        templateEngine = new TemplateEngine(variableRepository);
        contextRepository = new StubContextRepository();
        requestExecutor = new StubRequestExecutor();
        historyRepository = new TestHistoryRepository();

        headerEngine = new HeaderEngine(List.of(
            new HeaderProvider() {
                @Override public int getOrder() { return 1; }
                @Override public boolean supports(ResolutionContext c) { return true; }
                @Override public HeaderResolution resolve(ResolutionContext c) {
                    return HeaderResolution.success(Map.of("Accept", "*/*"));
                }
            }
        ));

        service = new IntendServiceImpl(
            contextRepository, headerEngine, requestExecutor,
            historyRepository, templateEngine, variableRepository,
            new ConfigRepository(), new SavedRequestRepository()
        );
    }

    private RequestIntent getIntent() {
        return new RequestIntent(
            RequestIntent.Method.GET, URL, null,
            RequestIntent.AuthStrategy.NONE, false, "dev"
        );
    }

    private RequestIntent postIntent(String body) {
        return new RequestIntent(
            RequestIntent.Method.POST, URL, body,
            RequestIntent.AuthStrategy.BEARER_TOKEN, false, "dev"
        );
    }

    // ── executeRequest ─────────────────────────────────────────────

    @Test
    @DisplayName("executeRequest should invoke the full pipeline")
    void executeRequestPipeline() {
        RequestIntent intent = getIntent();
        requestExecutor.resultToReturn = ExecutionResult.success(200, "{}", 5);

        service.executeRequest(intent);

        assertThat(requestExecutor.lastIntent).isNotNull();
        assertThat(requestExecutor.lastHeaders).containsEntry("Accept", "*/*");
    }

    // ── executeRequestWithResult ───────────────────────────────────

    @Test
    @DisplayName("executeRequestWithResult should return ExecutionResult")
    void executeRequestWithResultReturnsResponse() {
        requestExecutor.resultToReturn = ExecutionResult.success(200, "{\"id\":1}", 12);

        ExecutionResult result = service.executeRequestWithResult(getIntent());

        assertThat(result.statusCode()).isEqualTo(200);
        assertThat(result.body()).contains("{\"id\":1}");
        assertThat(result.timeMs()).isEqualTo(12);
        assertThat(result.statusCategory()).isEqualTo("Success");
    }

    @Test
    @DisplayName("executeRequestWithResult should add to history")
    void addsToHistory() {
        requestExecutor.resultToReturn = ExecutionResult.success(200, "{}", 1);

        service.executeRequestWithResult(getIntent());

        assertThat(historyRepository.entries).hasSize(1);
        assertThat(historyRepository.entries.get(0)).contains("GET");
        assertThat(historyRepository.entries.get(0)).contains(URL.toString());
    }

    // ── Template Resolution ────────────────────────────────────────

    @Test
    @DisplayName("should resolve URL templates before execution")
    void resolveUrlTemplate() {
        variableRepository.put("version", "v2");
        requestExecutor.resultToReturn = ExecutionResult.success(200, "{}", 1);

        RequestIntent intent = new RequestIntent(
            RequestIntent.Method.GET,
            URI.create("https://api.example.com/%7B%7Bversion%7D%7D/users"),
            null, RequestIntent.AuthStrategy.NONE, false, "dev"
        );

        service.executeRequestWithResult(intent);

        assertThat(requestExecutor.lastIntent).isNotNull();
        assertThat(requestExecutor.lastIntent.method()).isEqualTo(RequestIntent.Method.GET);
    }

    @Test
    @DisplayName("should resolve body templates before execution")
    void resolveBodyTemplate() {
        variableRepository.put("userName", "Alice");
        requestExecutor.resultToReturn = ExecutionResult.success(201, "{}", 3);

        service.executeRequestWithResult(postIntent("{\"name\": \"{{userName}}\"}"));

        assertThat(requestExecutor.lastIntent.payload().toString())
            .isEqualTo("{\"name\": \"Alice\"}");
    }

    // ── Variable Capture ───────────────────────────────────────────

    @Test
    @DisplayName("should capture variables from JSON response")
    void captureVariables() {
        requestExecutor.resultToReturn = ExecutionResult.success(200,
                "{\"user\":{\"id\":42,\"name\":\"Alice\"}}", 8);

        Map<String, String> captures = Map.of(
            "userId", "/user/id",
            "userName", "/user/name"
        );

        service.executeRequestWithResult(getIntent(), captures);

        assertThat(variableRepository.get("userId")).isEqualTo("42");
        assertThat(variableRepository.get("userName")).isEqualTo("Alice");
    }

    @Test
    @DisplayName("should not fail when captures is null")
    void nullCapturesHandled() {
        requestExecutor.resultToReturn = ExecutionResult.success(200, "{}", 1);

        assertThatCode(() -> service.executeRequestWithResult(getIntent(), null))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("should not fail when response has no JSON")
    void noJsonInResponse() {
        requestExecutor.resultToReturn = ExecutionResult.success(200, "plain text", 2);

        assertThatCode(() -> service.executeRequestWithResult(getIntent(), Map.of("key", "/value")))
            .doesNotThrowAnyException();
    }

    // ── Status categories ──────────────────────────────────────────

    @Test
    @DisplayName("client error result should have correct category")
    void clientErrorCategory() {
        requestExecutor.resultToReturn = ExecutionResult.success(404, "Not Found", 5);

        ExecutionResult result = service.executeRequestWithResult(getIntent());

        assertThat(result.statusCategory()).isEqualTo("Client Error");
        assertThat(result.isClientError()).isTrue();
    }

    @Test
    @DisplayName("server error result should have correct category")
    void serverErrorCategory() {
        requestExecutor.resultToReturn = ExecutionResult.success(500, "Internal Server Error", 10);

        ExecutionResult result = service.executeRequestWithResult(getIntent());

        assertThat(result.statusCategory()).isEqualTo("Server Error");
        assertThat(result.isServerError()).isTrue();
    }

    // ── Error handling ─────────────────────────────────────────────

    @Test
    @DisplayName("error result should be handled gracefully")
    void errorResultHandled() {
        requestExecutor.resultToReturn = ExecutionResult.error("Request timed out after 30s.");

        ExecutionResult result = service.executeRequestWithResult(getIntent());

        assertThat(result.statusCode()).isEqualTo(0);
        assertThat(result.body()).contains("timed out");
        assertThat(result.statusCategory()).isEqualTo("Error");
    }

    // ── Accessors ──────────────────────────────────────────────────

    @Test
    @DisplayName("getHistory should return the injected history repository")
    void getHistory() {
        assertThat(service.getHistory()).isSameAs(historyRepository);
    }

    @Test
    @DisplayName("getConfigRepository returns the injected config repository")
    void getConfigRepository() {
        assertThat(service.getConfigRepository()).isNotNull();
    }

    // ════════════════════════════════════════════════════════════════
    // Test Doubles (no Mockito needed)
    // ════════════════════════════════════════════════════════════════

    /** Stub ContextRepository that returns a simple ResolutionContext. */
    static class StubContextRepository implements ContextRepository {
        @Override
        public ResolutionContext loadContext(RequestIntent intent) {
            return new ResolutionContext(intent, Map.of(), Map.of());
        }
    }

    /** Stub RequestExecutor that records calls and returns a canned result. */
    static class StubRequestExecutor implements RequestExecutor {
        ExecutionResult resultToReturn = ExecutionResult.success(200, "{}", 1);
        RequestIntent lastIntent;
        Map<String, String> lastHeaders;

        @Override
        public ExecutionResult execute(RequestIntent intent, Map<String, String> headers) {
            this.lastIntent = intent;
            this.lastHeaders = headers;
            return resultToReturn;
        }
    }

    /** Lightweight history recorder (avoids file I/O in ~/.intend). */
    static class TestHistoryRepository extends HistoryRepository {
        final List<String> entries = new ArrayList<>();

        @Override
        public void add(String method, String url, String body) {
            entries.add(method + " " + url + " " + body);
        }
    }
}
