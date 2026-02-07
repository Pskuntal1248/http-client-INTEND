package com.intend.service.impl;

import com.intend.context.ResolutionContext;
import com.intend.core.RequestIntent;
import com.intend.engine.HeaderEngine;
import com.intend.repository.ContextRepository;
import com.intend.service.IntendService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class IntendServiceImpl implements IntendService {

    private final ContextRepository repository;
    private final HeaderEngine engine;

    public IntendServiceImpl(ContextRepository repository, HeaderEngine engine) {
        this.repository = repository;
        this.engine = engine;
    }

    @Override
    public Map<String, String> prepareRequest(RequestIntent intent) {
        ResolutionContext context = repository.loadContext(intent);
        return engine.execute(context);
    }
}
