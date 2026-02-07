package com.intend.service.impl;

import com.intend.context.ResolutionContext;
import com.intend.core.RequestIntent;
import com.intend.engine.HeaderEngine;
import com.intend.execution.RequestExecutor;
import com.intend.repository.ContextRepository;
import com.intend.service.IntendService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class IntendServiceImpl implements IntendService {

    private final ContextRepository repository;
    private final HeaderEngine engine;
    private final RequestExecutor executor;

    public IntendServiceImpl(ContextRepository repository, HeaderEngine engine, RequestExecutor executor) {
        this.repository = repository;
        this.engine = engine;
        this.executor = executor;
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
}
