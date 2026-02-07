package com.intend.providers;

import com.intend.context.ResolutionContext;
import com.intend.core.RequestIntent;
import com.intend.spi.HeaderProvider;

import java.util.HashMap;
import java.util.Map;

public class ApiKeyProvider implements HeaderProvider {

    @Override
    public boolean supports(ResolutionContext context) {
        return context.intent().auth() == RequestIntent.AuthStrategy.API_KEY;
    }

    @Override
    public Map<String, String> provide(ResolutionContext context) {
        Map<String, String> headers = new HashMap<>();
        String apiKey = context.secrets().getOrDefault("API_KEY", "MISSING_KEY");
        headers.put("X-API-KEY", apiKey);
        return headers;
    }
}
