package com.intend.repository;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;

@Repository
public class ConfigRepository {

    private final File file = DataDir.resolve("intend-config.json");
    private final ObjectMapper mapper = new ObjectMapper();
    private ConfigData cache = new ConfigData();

    public ConfigRepository() {
        load();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ConfigData {
        public String devUrl = "http://localhost:8080";
        public String devKey = "";
        public String devBearerToken = "";
        public String devBasicUser = "";
        public String devBasicPass = "";
        public String prodUrl = "https://api.example.com";
        public String prodKey = "";
        public String prodBearerToken = "";
        public String prodBasicUser = "";
        public String prodBasicPass = "";
    }

    public ConfigData get() {
        return cache;
    }

    public void save(String devUrl, String devKey, String devBearerToken, String devBasicUser, String devBasicPass,
                     String prodUrl, String prodKey, String prodBearerToken, String prodBasicUser, String prodBasicPass) {
        cache.devUrl = devUrl;
        cache.devKey = devKey;
        cache.devBearerToken = devBearerToken;
        cache.devBasicUser = devBasicUser;
        cache.devBasicPass = devBasicPass;
        cache.prodUrl = prodUrl;
        cache.prodKey = prodKey;
        cache.prodBearerToken = prodBearerToken;
        cache.prodBasicUser = prodBasicUser;
        cache.prodBasicPass = prodBasicPass;
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
