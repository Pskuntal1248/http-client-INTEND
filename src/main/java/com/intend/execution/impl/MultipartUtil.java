package com.intend.execution.impl;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MultipartUtil {

    private final String boundary;
    private final List<byte[]> byteArrays = new ArrayList<>();

    public MultipartUtil() {
        this.boundary = "---IntendBoundary" + UUID.randomUUID();
    }

    public String getBoundary() {
        return boundary;
    }

    public void addFormField(String name, String value) {
        String header = "--" + boundary + "\r\n"
            + "Content-Disposition: form-data; name=\"" + name + "\"\r\n"
            + "Content-Type: text/plain; charset=UTF-8\r\n\r\n";

        byteArrays.add(header.getBytes(StandardCharsets.UTF_8));
        byteArrays.add(value.getBytes(StandardCharsets.UTF_8));
        byteArrays.add("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    public void addFilePart(String fieldName, Path path) throws IOException {
        String filename = path.getFileName().toString();
        String mimeType = Files.probeContentType(path);
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }

        String header = "--" + boundary + "\r\n"
            + "Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + filename + "\"\r\n"
            + "Content-Type: " + mimeType + "\r\n\r\n";

        byteArrays.add(header.getBytes(StandardCharsets.UTF_8));
        byteArrays.add(Files.readAllBytes(path));
        byteArrays.add("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    public HttpRequest.BodyPublisher build() {
        byteArrays.add(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        return HttpRequest.BodyPublishers.ofByteArrays(byteArrays);
    }
}
