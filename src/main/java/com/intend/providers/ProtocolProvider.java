package com.intend.providers;

import com.intend.context.ResolutionContext;
import com.intend.spi.HeaderProvider;

import java.util.HashMap;
import java.util.Map;

public class ProtocolProvider implements HeaderProvider {

    @Override
    public boolean supports(ResolutionContext context) {
        return true; // Always applies
    }

    @Override
    public Map<String, String> provide(ResolutionContext context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "*/*");
        headers.put("Content-Type", "application/json");
        return headers;
    }
}
