package com.intend.repository.impl;

import com.intend.context.ResolutionContext;
import com.intend.core.RequestIntent;
import com.intend.repository.ConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.*;

@DisplayName("EnvContextRepository – Environment Context Loading Tests")
class EnvContextRepositoryTest {

    private ConfigRepository configRepo;
    private EnvContextRepository contextRepo;
    private static final URI URL = URI.create("https://api.example.com/test");

    @BeforeEach
    void setUp() {
        configRepo = new ConfigRepository();
        // Set known config values
        configRepo.save("http://localhost:3000", "dev-key-123", "", "", "", "https://prod.api.com", "prod-key-456", "", "", "");
        contextRepo = new EnvContextRepository(configRepo);
    }

    private RequestIntent intent(String env) {
        return new RequestIntent(
            RequestIntent.Method.GET, URL, null,
            RequestIntent.AuthStrategy.API_KEY, false, env
        );
    }

    @Test
    @DisplayName("should load dev context by default")
    void devContext() {
        ResolutionContext ctx = contextRepo.loadContext(intent("dev"));

        assertThat(ctx.intent()).isNotNull();
        assertThat(ctx.config()).containsEntry("BASE_URL", "http://localhost:3000");
        assertThat(ctx.config()).containsEntry("ENV", "dev");
        assertThat(ctx.secrets()).containsEntry("API_KEY", "dev-key-123");
    }

    @Test
    @DisplayName("should load prod context for 'prod' env")
    void prodContext() {
        ResolutionContext ctx = contextRepo.loadContext(intent("prod"));

        assertThat(ctx.config()).containsEntry("BASE_URL", "https://prod.api.com");
        assertThat(ctx.config()).containsEntry("ENV", "prod");
        assertThat(ctx.secrets()).containsEntry("API_KEY", "prod-key-456");
    }

    @Test
    @DisplayName("should load prod context for 'PROD' (case-insensitive)")
    void prodContextCaseInsensitive() {
        ResolutionContext ctx = contextRepo.loadContext(intent("PROD"));

        assertThat(ctx.config()).containsEntry("ENV", "prod");
    }

    @Test
    @DisplayName("should default to dev for unknown env value")
    void unknownEnvDefaultsToDev() {
        ResolutionContext ctx = contextRepo.loadContext(intent("staging"));

        assertThat(ctx.config()).containsEntry("ENV", "dev");
    }
}
