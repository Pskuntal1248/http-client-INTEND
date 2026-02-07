package com.intend.context;

import com.intend.core.RequestIntent;
import java.util.Map;

public record ResolutionContext(
    RequestIntent intent,
    Map<String, String> config,
    Map<String, String> secrets
) {}
