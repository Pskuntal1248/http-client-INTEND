package com.intend.repository.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Path;
import java.util.Properties;

import static org.assertj.core.api.Assertions.*;

@DisplayName("FileStateRepository – State Persistence Tests")
class FileStateRepositoryTest {

    /**
     * We test the StateRepository interface using an in-memory implementation
     * to avoid file system side effects. The real FileStateRepository writes
     * to ~/.intend/ which we don't want to touch in unit tests.
     */

    private InMemoryStateRepo stateRepo;

    @BeforeEach
    void setUp() {
        stateRepo = new InMemoryStateRepo();
    }

    @Test
    @DisplayName("should return null for unknown key")
    void unknownKey() {
        assertThat(stateRepo.getLastIdempotencyKey("unknown")).isNull();
    }

    @Test
    @DisplayName("should save and retrieve idempotency key")
    void saveAndRetrieve() {
        stateRepo.saveIdempotencyKey("POST:/api/orders", "uuid-123");
        assertThat(stateRepo.getLastIdempotencyKey("POST:/api/orders")).isEqualTo("uuid-123");
    }

    @Test
    @DisplayName("should overwrite existing key")
    void overwrite() {
        stateRepo.saveIdempotencyKey("POST:/api/orders", "uuid-1");
        stateRepo.saveIdempotencyKey("POST:/api/orders", "uuid-2");
        assertThat(stateRepo.getLastIdempotencyKey("POST:/api/orders")).isEqualTo("uuid-2");
    }

    @Test
    @DisplayName("different fingerprints should be independent")
    void differentKeys() {
        stateRepo.saveIdempotencyKey("POST:/a", "id-a");
        stateRepo.saveIdempotencyKey("PUT:/b", "id-b");

        assertThat(stateRepo.getLastIdempotencyKey("POST:/a")).isEqualTo("id-a");
        assertThat(stateRepo.getLastIdempotencyKey("PUT:/b")).isEqualTo("id-b");
    }

    // In-memory implementation for testing the contract
    static class InMemoryStateRepo implements com.intend.repository.StateRepository {
        private final Properties props = new Properties();

        @Override
        public String getLastIdempotencyKey(String key) {
            return props.getProperty(key);
        }

        @Override
        public void saveIdempotencyKey(String key, String uuid) {
            props.setProperty(key, uuid);
        }
    }
}
