package com.intend.engine;

import com.intend.context.ResolutionContext;
import com.intend.spi.HeaderProvider;
import com.intend.spi.HeaderResolution;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HeaderEngine {

    private final List<HeaderProvider> providers;

    public HeaderEngine(List<HeaderProvider> providers) {
        this.providers = providers.stream()
                .sorted(Comparator.comparingInt(HeaderProvider::getOrder))
                .toList();
    }

    public Map<String, String> execute(ResolutionContext context) {
        Map<String, String> headers = new LinkedHashMap<>();
        for (HeaderProvider provider : providers) {
            if (provider.supports(context)) {
                HeaderResolution resolution = provider.resolve(context);
                if (resolution.success()) {
                    headers.putAll(resolution.headers());
                } else {
                    System.err.println("⚠️ Provider failed: " + resolution.errorMessage());
                }
            }
        }
        return headers;
    }
}
