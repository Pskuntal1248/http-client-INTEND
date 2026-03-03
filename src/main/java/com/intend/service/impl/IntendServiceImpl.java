package com.intend.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intend.context.ResolutionContext;
import com.intend.core.MultipartPayload;
import com.intend.core.RequestIntent;
import com.intend.engine.HeaderEngine;
import com.intend.engine.TemplateEngine;
import com.intend.execution.ExecutionResult;
import com.intend.execution.RequestExecutor;
import com.intend.repository.ConfigRepository;
import com.intend.repository.ContextRepository;
import com.intend.repository.HistoryRepository;
import com.intend.repository.VariableRepository;
import com.intend.service.IntendService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URI;
import java.util.Map;

@Service
public class IntendServiceImpl implements IntendService {
    private final ContextRepository repository;
    private final HeaderEngine engine;
    private final RequestExecutor executor;
    private final HistoryRepository historyRepository;
    private final TemplateEngine templateEngine;
    private final VariableRepository variableRepository;
    private final ConfigRepository configRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    public IntendServiceImpl(
        ContextRepository repository,
        HeaderEngine engine,
        RequestExecutor executor,
        HistoryRepository historyRepository,
        TemplateEngine templateEngine,
        VariableRepository variableRepository,
        ConfigRepository configRepository
    ) {
        this.repository = repository;
        this.engine = engine;
        this.executor = executor;
        this.historyRepository = historyRepository;
        this.templateEngine = templateEngine;
        this.variableRepository = variableRepository;
        this.configRepository = configRepository;
    }

    @Override
    public void executeRequest(RequestIntent intent) {
        ResolutionContext context = repository.loadContext(intent);
        Map<String, String> headers = engine.execute(context);
        ExecutionResult result = executor.execute(intent, headers);

        System.out.println("\nResponse Received:");
        System.out.println(result.toPrettyString());
    }

    @Override
    public ExecutionResult executeRequestWithResult(RequestIntent intent) {
        return executeRequestWithResult(intent, null);
    }

    @Override
    public ExecutionResult executeRequestWithResult(RequestIntent intent, Map<String, String> captures) {
        RequestIntent resolvedIntent = resolveIntent(intent);

        historyRepository.add(
            resolvedIntent.method().name(),
            resolvedIntent.url().toString(),
            resolvedIntent.payload() != null ? resolvedIntent.payload().toString() : ""
        );

        ResolutionContext context = repository.loadContext(resolvedIntent);
        Map<String, String> headers = engine.execute(context);
        ExecutionResult result = executor.execute(resolvedIntent, headers);

        if (result.statusCode() > 0 && result.body() != null) {
            captureVariables(result.body(), captures);
        }

        return result;
    }

    private RequestIntent resolveIntent(RequestIntent intent) {
        Object processedBody = resolvePayload(intent.payload());
        String processedUrl = templateEngine.process(intent.url().toString());

        return new RequestIntent(
            intent.method(),
            URI.create(processedUrl),
            processedBody,
            intent.auth(),
            intent.forceNew(),
            intent.env()
        );
    }

    private Object resolvePayload(Object payload) {
        if (payload instanceof MultipartPayload mp) {
            String resolvedBody = mp.hasBody() ? templateEngine.process(mp.body()) : null;
            return new MultipartPayload(mp.file(), resolvedBody);
        }

        if (payload instanceof File) {
            return payload;
        }

        return templateEngine.process(payload == null ? null : payload.toString());
    }

    private void captureVariables(String responseBody, Map<String, String> captures) {
        if (captures == null || captures.isEmpty()) {
            return;
        }

        try {
            String json = extractJson(responseBody);
            JsonNode root = mapper.readTree(json);
            for (Map.Entry<String, String> entry : captures.entrySet()) {
                JsonNode valueNode = root.at(entry.getValue());
                if (!valueNode.isMissingNode()) {
                    variableRepository.put(entry.getKey(), valueNode.asText());
                }
            }
        } catch (Exception e) {
            System.err.println("Extraction Failed: " + e.getMessage());
        }
    }

    private String extractJson(String text) {
        if (text != null && text.contains("{")) {
            return text.substring(text.indexOf('{'));
        }
        return "{}";
    }

    public HistoryRepository getHistory() {
        return historyRepository;
    }

    public ConfigRepository getConfigRepository() {
        return configRepository;
    }
}
