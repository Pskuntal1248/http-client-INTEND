package com.intend.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class SavedRequestRepository {

    private final File file = DataDir.resolve("saved-requests.json");
    private final ObjectMapper mapper = new ObjectMapper();
    private final List<SavedRequest> cache = new ArrayList<>();

    public record SavedRequest(
        String name,
        String method,
        String url,
        String body,
        String auth,
        String env,
        Map<String, String> params
    ) {
        @Override
        public String toString() {
            return name;
        }

        public Map<String, Object> toShareableMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("name", name);
            map.put("method", method);
            map.put("url", url);
            if (body != null && !body.isBlank()) {
                map.put("body", body);
            }
            if (auth != null && !"NONE".equals(auth)) {
                map.put("auth", auth);
            }
            if (env != null) {
                map.put("env", env);
            }
            if (params != null && !params.isEmpty()) {
                map.put("params", params);
            }
            return map;
        }
    }

    public SavedRequestRepository() {
        load();
    }

    public void save(SavedRequest request) {
        cache.removeIf(r -> r.name().equals(request.name()));
        cache.add(0, request);
        persist();
    }

    public List<SavedRequest> getAll() {
        return new ArrayList<>(cache);
    }

    public void delete(SavedRequest request) {
        cache.remove(request);
        persist();
    }

    public String toJson(SavedRequest request) {
        try {
            return mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(request.toShareableMap());
        } catch (IOException e) {
            return "{}";
        }
    }

    private void persist() {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, cache);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void load() {
        if (file.exists()) {
            try {
                List<SavedRequest> loaded = mapper.readValue(file, new TypeReference<>() {});
                cache.addAll(loaded);
            } catch (IOException e) {
                System.err.println("Could not load saved-requests.json");
            }
        }
    }
}
