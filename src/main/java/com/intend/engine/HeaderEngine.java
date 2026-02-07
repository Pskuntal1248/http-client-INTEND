package com.intend.engine;

import com.intend.context.ResolutionContext;
import com.intend.spi.HeaderProvider;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HeaderEngine {

    private final List<HeaderProvider> providers;

    public HeaderEngine(List<HeaderProvider> providers) {
        this.providers = providers;
    }

    public Map<String, String> execute(ResolutionContext context) {
        Map<String, String> headers = new LinkedHashMap<>();
        for (HeaderProvider provider : providers) {
            if (provider.supports(context)) {
                headers.putAll(provider.provide(context));
            }
        }
        return headers;
    }
}
