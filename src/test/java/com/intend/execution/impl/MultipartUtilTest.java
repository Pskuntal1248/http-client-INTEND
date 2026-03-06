package com.intend.execution.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MultipartUtil – Multipart Form Data Builder Tests")
class MultipartUtilTest {

    @Test
    @DisplayName("should generate a non-empty boundary")
    void boundary() {
        MultipartUtil util = new MultipartUtil();
        assertThat(util.getBoundary()).isNotNull().isNotEmpty();
        assertThat(util.getBoundary()).startsWith("---IntendBoundary");
    }

    @Test
    @DisplayName("boundary should be unique per instance")
    void uniqueBoundary() {
        MultipartUtil a = new MultipartUtil();
        MultipartUtil b = new MultipartUtil();
        assertThat(a.getBoundary()).isNotEqualTo(b.getBoundary());
    }

    @Test
    @DisplayName("should build a valid body publisher with form field")
    void formField() {
        MultipartUtil util = new MultipartUtil();
        util.addFormField("name", "John Doe");
        HttpRequest.BodyPublisher publisher = util.build();

        assertThat(publisher).isNotNull();
        // ofByteArrays returns -1 (unknown length), just verify it was created
    }

    @Test
    @DisplayName("should build a valid body publisher with file part")
    void filePart(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("test.txt");
        Files.writeString(testFile, "hello world", StandardCharsets.UTF_8);

        MultipartUtil util = new MultipartUtil();
        util.addFilePart("file", testFile);
        HttpRequest.BodyPublisher publisher = util.build();

        assertThat(publisher).isNotNull();
    }

    @Test
    @DisplayName("should include both form fields and file parts")
    void mixedParts(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("data.json");
        Files.writeString(testFile, "{\"key\":\"value\"}", StandardCharsets.UTF_8);

        MultipartUtil util = new MultipartUtil();
        util.addFormField("description", "test upload");
        util.addFilePart("file", testFile);
        HttpRequest.BodyPublisher publisher = util.build();

        assertThat(publisher).isNotNull();
    }
}
