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

        if ("prod".equalsIgnoreCase(intent.env())) {
            return new ResolutionContext(
                intent,
                Map.of(
                    "BASE_URL", data.prodUrl,
                    "ENV", "prod"
                ),
                Map.of(
                    "API_KEY", data.prodKey
                )
            );
        }

        return new ResolutionContext(
            intent,
            Map.of(
                "BASE_URL", data.devUrl,
                "ENV", "dev"
            ),
            Map.of(
                "API_KEY", data.devKey
            )
        );
    }
}
