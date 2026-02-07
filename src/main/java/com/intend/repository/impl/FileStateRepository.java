package com.intend.repository.impl;

import com.intend.repository.StateRepository;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.Properties;

@Repository
public class FileStateRepository implements StateRepository {

    private final File file;
    private final Properties props;

    public FileStateRepository() {
        this.file = new File(".intend-state.properties");
        this.props = new Properties();
        load();
    }

    private void load() {
        if (file.exists()) {
            try (InputStream in = new FileInputStream(file)) {
                props.load(in);
            } catch (IOException e) {
                System.err.println("⚠️ Warning: Could not load state file.");
            }
        }
    }

    private void save() {
        try (OutputStream out = new FileOutputStream(file)) {
            props.store(out, "Intend Execution State");
        } catch (IOException e) {
            System.err.println("❌ Error: Could not save state.");
        }
    }

    @Override
    public String getLastIdempotencyKey(String key) {
        return props.getProperty(key);
    }

    @Override
    public void saveIdempotencyKey(String key, String uuid) {
        props.setProperty(key, uuid);
        save();
    }
}
