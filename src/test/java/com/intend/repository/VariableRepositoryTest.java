package com.intend.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class VariableRepositoryTest {

    private VariableRepository repository;

    @BeforeEach
    void setUp() {
        repository = new VariableRepository();
    }

    @Test
    void shouldStoreAndRetrieveVariable() {
        repository.put("USER_ID", "42");
        assertEquals("42", repository.get("USER_ID"));
    }

    @Test
    void shouldReturnNullForMissingKey() {
        assertNull(repository.get("NONEXISTENT"));
    }

    @Test
    void shouldOverwriteExistingVariable() {
        repository.put("TOKEN", "old");
        repository.put("TOKEN", "new");
        assertEquals("new", repository.get("TOKEN"));
    }

    @Test
    void shouldReturnAllVariables() {
        repository.put("A", "1");
        repository.put("B", "2");
        Map<String, String> all = repository.getAll();
        assertEquals(2, all.size());
        assertEquals("1", all.get("A"));
        assertEquals("2", all.get("B"));
    }

    @Test
    void shouldReturnDefensiveCopyFromGetAll() {
        repository.put("KEY", "value");
        Map<String, String> all = repository.getAll();
        all.put("INJECTED", "hacked");
        assertNull(repository.get("INJECTED"));
    }
}
