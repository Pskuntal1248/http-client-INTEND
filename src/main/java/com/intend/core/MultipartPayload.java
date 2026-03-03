package com.intend.core;

import java.io.File;

/**
 * Represents a multipart request payload containing a file
 * and optional text fields (JSON metadata, form fields, etc.).
 */
public record MultipartPayload(
    File file,
    String body
) {
    public boolean hasBody() {
        return body != null && !body.isBlank();
    }
}
