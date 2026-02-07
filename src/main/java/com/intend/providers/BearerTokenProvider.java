package com.intend.providers;

import com.intend.context.ResolutionContext;
import com.intend.core.RequestIntent;
import com.intend.spi.HeaderProvider;
import com.intend.spi.HeaderResolution;

import java.util.Map;

public class BearerTokenProvider implements HeaderProvider {

    @Override
    public int getOrder() {
        return 91;
    }

    @Override
    public boolean supports(ResolutionContext ctx) {
        return ctx.intent().auth() == RequestIntent.AuthStrategy.BEARER_TOKEN;
    }

    @Override
    public HeaderResolution resolve(ResolutionContext ctx) {
        String token = ctx.secrets().get("ACCESS_TOKEN");
        if (token == null) {
            token = "ey...mock_jwt_token...";
        }

        return HeaderResolution.success(Map.of(
            "Authorization", "Bearer " + token
        ));
    }
}
