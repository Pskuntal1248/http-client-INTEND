package com.intend.execution;

import com.intend.core.RequestIntent;
import java.util.Map;

public interface RequestExecutor {
    /**
     * Executes the HTTP request and returns the raw response body.
     */
    String execute(RequestIntent intent, Map<String, String> headers);
}
