package com.intend.repository.impl;

import com.intend.context.ResolutionContext;
import com.intend.core.RequestIntent;
import com.intend.repository.ConfigRepository;
import com.intend.repository.ContextRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class EnvContextRepository implements ContextRepository {
    private final ConfigRepository configRepository;

    public EnvContextRepository(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    @Override
    public ResolutionContext loadContext(RequestIntent intent) {
        ConfigRepository.ConfigData data = configRepository.get();
        boolean isProd = "prod".equalsIgnoreCase(intent.env());

        Map<String, String> config = Map.of(
            "BASE_URL", isProd ? data.prodUrl : data.devUrl,
            "ENV", isProd ? "prod" : "dev"
        );

        Map<String, String> secrets = new java.util.HashMap<>();
        secrets.put("API_KEY", isProd ? data.prodKey : data.devKey);
        secrets.put("ACCESS_TOKEN", isProd ? data.prodBearerToken : data.devBearerToken);
        secrets.put("BASIC_USER", isProd ? data.prodBasicUser : data.devBasicUser);
        secrets.put("BASIC_PASS", isProd ? data.prodBasicPass : data.devBasicPass);

        return new ResolutionContext(intent, config, secrets);
    }
}
