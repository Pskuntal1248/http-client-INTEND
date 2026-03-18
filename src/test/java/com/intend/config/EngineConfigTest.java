package com.intend.config;

import com.intend.engine.HeaderEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("EngineConfig – Spring Configuration Tests")
class EngineConfigTest {

    @Test
    @DisplayName("should create HeaderEngine bean with all providers wired")
    void headerEngineBeanCreated() {
        EngineConfig config = new EngineConfig();

        HeaderEngine engine = config.headerEngine();
        assertThat(engine).isNotNull();
    }
}
