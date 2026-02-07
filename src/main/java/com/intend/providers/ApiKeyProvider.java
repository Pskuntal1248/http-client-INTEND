package com.intend.providers;

import com.intend.context.ResolutionContext;
import com.intend.core.RequestIntent;
import com.intend.spi.HeaderProvider;
import com.intend.spi.HeaderResolution;

import java.util.Map;

public class ApiKeyProvider implements HeaderProvider {

    @Override
    public int getOrder() {
        return 90;
    }

    @Override
    public boolean supports(ResolutionContext context) {
        return context.intent().auth() == RequestIntent.AuthStrategy.API_KEY;
    }

    @Override
    public HeaderResolution resolve(ResolutionContext context) {
        String apiKey = context.secrets().getOrDefault("API_KEY", "MISSING_KEY");
        return HeaderResolution.success(Map.of("X-API-KEY", apiKey));
    }
}
