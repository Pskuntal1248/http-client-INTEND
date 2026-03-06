package com.intend.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

class DataDirTest {

    @Test
    void root_returns_directory_inside_user_home() {
        File root = DataDir.root();
        assertThat(root.getName()).isEqualTo(".intend");
        assertThat(root.getParent()).isEqualTo(System.getProperty("user.home"));
    }

    @Test
    void resolve_returns_file_inside_data_dir() {
        File f = DataDir.resolve("history.json");
        assertThat(f.getName()).isEqualTo("history.json");
        assertThat(f.getParentFile().getName()).isEqualTo(".intend");
    }

    @Test
    void resolve_different_filenames() {
        File a = DataDir.resolve("intend-config.json");
        File b = DataDir.resolve("intend-state.properties");
        assertThat(a.getName()).isEqualTo("intend-config.json");
        assertThat(b.getName()).isEqualTo("intend-state.properties");
        assertThat(a.getParentFile()).isEqualTo(b.getParentFile());
    }
}
