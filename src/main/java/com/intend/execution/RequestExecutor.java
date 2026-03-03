package com.intend.execution;

import com.intend.core.RequestIntent;
import java.util.Map;

public interface RequestExecutor {
    ExecutionResult execute(RequestIntent intent, Map<String, String> headers);
}
