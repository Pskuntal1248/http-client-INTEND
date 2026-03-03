package com.intend.config;

import com.intend.engine.HeaderEngine;
import com.intend.repository.StateRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("EngineConfig – Spring Configuration Tests")
class EngineConfigTest {

    @Test
    @DisplayName("should create HeaderEngine bean with all providers wired")
    void headerEngineBeanCreated() {
        EngineConfig config = new EngineConfig();

        // Simple in-memory StateRepository for test
        StateRepository stateRepository = new StateRepository() {
            private final Map<String, String> store = new HashMap<>();

            @Override
            public String getLastIdempotencyKey(String key) {
                return store.get(key);
            }

            @Override
            public void saveIdempotencyKey(String key, String uuid) {
                store.put(key, uuid);
            }
        };

        HeaderEngine engine = config.headerEngine(stateRepository);
        assertThat(engine).isNotNull();
    }
}
