package com.intend.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Repository
public class HistoryRepository {

    private final File file = new File("history.json");
    private final ObjectMapper mapper = new ObjectMapper();
    private final List<HistoryItem> cache = new ArrayList<>();

    public record HistoryItem(String method, String url, String body, String timestamp) {
        @Override
        public String toString() {
            return method + " " + url;
        }
    }

    public HistoryRepository() {
        load();
    }

    public void add(String method, String url, String body) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        cache.add(0, new HistoryItem(method, url, body, time));
        save();
    }

    public List<HistoryItem> getAll() {
        return new ArrayList<>(cache);
    }

    public void delete(HistoryItem item) {
        cache.remove(item);
        save();
    }

    private void save() {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, cache);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void load() {
        if (file.exists()) {
            try {
                List<HistoryItem> loaded = mapper.readValue(file, new TypeReference<>() {});
                cache.addAll(loaded);
            } catch (IOException e) {
                System.err.println("Could not load history.json");
            }
        }
    }
}
