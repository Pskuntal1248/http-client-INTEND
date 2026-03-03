package com.intend.service;

import com.intend.core.RequestIntent;
import com.intend.execution.ExecutionResult;

import java.util.Map;

public interface IntendService {
    void executeRequest(RequestIntent intent);
    ExecutionResult executeRequestWithResult(RequestIntent intent);
    ExecutionResult executeRequestWithResult(RequestIntent intent, Map<String, String> captures);
}
