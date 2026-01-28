package com.intend.core;

import java.util.Set;

public final class IntentValidator {
    private static final Set<String> ALLOWED_METHODS = Set.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
    private static final Set<String> ALLOWED_AUTH = Set.of("jwt", "oauth", "apikey", "none");

    public static void validate(RequestIntent intent){
        if (!ALLOWED_METHODS.contains(intent.getMethod())) {
            throw new IllegalArgumentException("Error: Invalid HTTP method '" + intent.getMethod() + "'. Allowed: " + ALLOWED_METHODS);
        }
        if (!intent.getUri().isAbsolute()) {
            throw new IllegalArgumentException("Error: URL must be absolute (e.g., https://api.com/...)");
        }
        if (!ALLOWED_AUTH.contains(intent.getAuthType())) {
            throw new IllegalArgumentException("Error: Invalid auth strategy '" + intent.getAuthType() + "'. Allowed: " + ALLOWED_AUTH);
        }
    }
}
