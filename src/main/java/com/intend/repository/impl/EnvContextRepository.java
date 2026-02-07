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

        if (System.getenv("INTEND_API_KEY") != null) {
            secrets.put("API_KEY", System.getenv("INTEND_API_KEY"));
        } else {
            secrets.put("API_KEY", "dev_secret_123");
        }

        Map<String, String> config = Map.of("ENV", "DEV", "REGION", "us-east-1");

        return new ResolutionContext(intent, config, secrets);
    }
}
