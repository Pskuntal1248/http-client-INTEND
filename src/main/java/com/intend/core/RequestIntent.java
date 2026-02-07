package com.intend.core;

import java.net.URI;

public record RequestIntent(
    Method method,
    URI url,
    String payload,
    AuthStrategy auth
) {
    public enum Method {
        GET, POST, PUT, DELETE, PATCH
    }

    public enum AuthStrategy {
        NONE, API_KEY, BEARER, OAUTH
    }
}
