package com.intend.providers;

import com.intend.context.ResolutionContext;
import com.intend.repository.StateRepository;
import com.intend.spi.HeaderProvider;
import com.intend.spi.HeaderResolution;

import java.util.Map;
import java.util.UUID;

public class IdempotencyProvider implements HeaderProvider {

    private final StateRepository stateRepository;

    public IdempotencyProvider(StateRepository stateRepository) {
        this.stateRepository = stateRepository;
    }

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
        String fingerprint = ctx.intent().method() + ":" + ctx.intent().url();

       
        if (ctx.intent().forceNew()) {
            System.out.println("Force New: Generating fresh Idempotency Key.");
            String newKey = UUID.randomUUID().toString();
            stateRepository.saveIdempotencyKey(fingerprint, newKey);

            return HeaderResolution.success(Map.of(
                "Idempotency-Key", newKey,
                "X-Request-ID", newKey
            ));
        }

        String existingKey = stateRepository.getLastIdempotencyKey(fingerprint);

        String finalKey;
        if (existingKey != null) {
            System.out.println("Memory: Reusing previous Idempotency Key for safety.");
            finalKey = existingKey;
        } else {
            finalKey = UUID.randomUUID().toString();
            stateRepository.saveIdempotencyKey(fingerprint, finalKey);
        }

        return HeaderResolution.success(Map.of(
            "Idempotency-Key", finalKey,
            "X-Request-ID", finalKey
        ));
    }
}
