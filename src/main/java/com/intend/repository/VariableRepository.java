package com.intend.repository;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class VariableRepository {

    private final Map<String, String> variables = new HashMap<>();

    public void put(String key, String value) {
        variables.put(key, value);
        System.out.println("Captured Variable: " + key + " = " + value);
    }

    public String get(String key) {
        return variables.get(key);
    }

    public Map<String, String> getAll() {
        return new HashMap<>(variables);
    }
}
