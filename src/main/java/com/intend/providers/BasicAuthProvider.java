package com.intend.providers;

import com.intend.context.ResolutionContext;
import com.intend.core.RequestIntent;
import com.intend.spi.HeaderProvider;
import com.intend.spi.HeaderResolution;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public class BasicAuthProvider implements HeaderProvider {

    @Override
    public int getOrder() {
        return 90;
    }

    @Override
    public boolean supports(ResolutionContext ctx) {
        return ctx.intent().auth() == RequestIntent.AuthStrategy.BASIC_AUTH;
    }

    @Override
    public HeaderResolution resolve(ResolutionContext ctx) {
        String user = ctx.secrets().getOrDefault("BASIC_USER", "admin");
        String pass = ctx.secrets().getOrDefault("BASIC_PASS", "password");

        String auth = user + ":" + pass;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        return HeaderResolution.success(Map.of(
            "Authorization", "Basic " + encodedAuth
        ));
    }
}
