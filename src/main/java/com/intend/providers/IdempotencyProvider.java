package com.intend.providers;

import com.intend.context.ResolutionContext;
import com.intend.spi.HeaderProvider;
import com.intend.spi.HeaderResolution;

import java.util.Map;
import java.util.UUID;

/**
 * Stripe-style idempotency: fresh key for every new request,
 * same key reused only during automatic retries (handled by the executor).
 */
public class IdempotencyProvider implements HeaderProvider {

    @Override
    public int getOrder() {
        return 50;
    }

    @Override
    public boolean supports(ResolutionContext ctx) {
        String method = ctx.intent().method().name();
        return method.equals("POST") || method.equals("PATCH") || method.equals("PUT");
    }

    @Override
    public HeaderResolution resolve(ResolutionContext ctx) {
        String key = UUID.randomUUID().toString();
        return HeaderResolution.success(Map.of(
            "Idempotency-Key", key,
            "X-Request-ID", key
        ));
    }
}
