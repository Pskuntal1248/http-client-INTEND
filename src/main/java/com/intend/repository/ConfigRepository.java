package com.intend.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;

@Repository
public class ConfigRepository {

    private final File file = new File("intend-config.json");
    private final ObjectMapper mapper = new ObjectMapper();
    private ConfigData cache = new ConfigData();

    public ConfigRepository() {
        load();
    }

    public static class ConfigData {
        public String devUrl = "http://localhost:8080";
        public String devKey = "";
        public String prodUrl = "https://api.example.com";
        public String prodKey = "";
    }

    public ConfigData get() {
        return cache;
    }

    public void save(String devUrl, String devKey, String prodUrl, String prodKey) {
        cache.devUrl = devUrl;
        cache.devKey = devKey;
        cache.prodUrl = prodUrl;
        cache.prodKey = prodKey;
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, cache);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void load() {
        if (file.exists()) {
            try {
                cache = mapper.readValue(file, ConfigData.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
