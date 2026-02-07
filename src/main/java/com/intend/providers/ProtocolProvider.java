package com.intend.providers;

import com.intend.context.ResolutionContext;
import com.intend.spi.HeaderProvider;
import com.intend.spi.HeaderResolution;

import java.util.Map;

public class ProtocolProvider implements HeaderProvider {

    @Override
    public int getOrder() {
        return 10;
    }

    @Override
    public boolean supports(ResolutionContext context) {
        return true;
    }

    @Override
    public HeaderResolution resolve(ResolutionContext context) {
        return HeaderResolution.success(Map.of(
            "Accept", "*/*",
            "Content-Type", "application/json"
        ));
    }
}
