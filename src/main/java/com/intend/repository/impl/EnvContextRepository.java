package com.intend.repository.impl;

import com.intend.context.ResolutionContext;
import com.intend.core.RequestIntent;
import com.intend.repository.ContextRepository;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class EnvContextRepository implements ContextRepository {
    @Override
    public ResolutionContext loadContext(RequestIntent intent) {
        Map<String, String> secrets = new HashMap<>();
        Map<String, String> config = new HashMap<>();
        String prefix = intent.env().toUpperCase() + "_";
        String apiKeyVar = prefix + "API_KEY";
        String value = System.getenv(apiKeyVar);
        if (value == null) {
            value = intent.env().equals("prod") ? "prod_live_999" : "dev_secret_123";
        }
        secrets.put("API_KEY", value);
        config.put("ENV", intent.env());
        System.out.println("Context Loaded: " + intent.env().toUpperCase() + " (Key: " + apiKeyVar + ")");
        return new ResolutionContext(intent, config, secrets);
    }
}
