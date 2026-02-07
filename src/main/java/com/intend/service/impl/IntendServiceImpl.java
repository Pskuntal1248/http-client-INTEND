package com.intend.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intend.context.ResolutionContext;
import com.intend.core.RequestIntent;
import com.intend.engine.HeaderEngine;
import com.intend.engine.TemplateEngine;
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
        // 1. Context
        ResolutionContext context = repository.loadContext(intent);

        // 2. Brain (Headers)
        Map<String, String> headers = engine.execute(context);

        // 3. Hands (Execution)
        String response = executor.execute(intent, headers);

        // 4. Output Result
        System.out.println("\nResponse Received:");
        System.out.println(response);
    }

    @Override
    public String executeRequestAsString(RequestIntent intent) {
        return executeRequestAsString(intent, null);
    }

    @Override
    public String executeRequestAsString(RequestIntent intent, Map<String, String> captures) {
        RequestIntent resolvedIntent = resolveIntent(intent);

        historyRepository.add(
            resolvedIntent.method().name(),
            resolvedIntent.url().toString(),
            resolvedIntent.payload() != null ? resolvedIntent.payload().toString() : ""
        );

        ResolutionContext context = repository.loadContext(resolvedIntent);
        Map<String, String> headers = engine.execute(context);
        String rawResponse = executor.execute(resolvedIntent, headers);

        captureVariables(rawResponse, captures);

        return rawResponse;
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
        if (payload instanceof File) {
            return payload;
        }

        return templateEngine.process(payload == null ? null : payload.toString());
    }

    private void captureVariables(String rawResponse, Map<String, String> captures) {
        if (captures == null || captures.isEmpty()) {
            return;
        }

        try {
            JsonNode root = mapper.readTree(extractJsonFromBody(rawResponse));
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

    private String extractJsonFromBody(String rawResponse) {
        if (rawResponse != null && rawResponse.contains("{")) {
            return rawResponse.substring(rawResponse.indexOf('{'));
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
