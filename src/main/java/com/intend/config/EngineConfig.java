package com.intend.config;

import com.intend.engine.HeaderEngine;
import com.intend.providers.ApiKeyProvider;
import com.intend.providers.BasicAuthProvider;
import com.intend.providers.BearerTokenProvider;
import com.intend.providers.IdempotencyProvider;
import com.intend.providers.ProtocolProvider;
import com.intend.repository.StateRepository;
import com.intend.spi.HeaderProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class EngineConfig {

    @Bean
    public HeaderEngine headerEngine(StateRepository stateRepository) {
        List<HeaderProvider> providers = List.of(
            new ProtocolProvider(),
            new ApiKeyProvider(),
            new BasicAuthProvider(),
            new BearerTokenProvider(),
            new IdempotencyProvider(stateRepository)
        );
        return new HeaderEngine(providers);
    }
}
