package com.intend.spi;

import com.intend.context.ResolutionContext;
import java.util.Map;

public interface HeaderProvider {
    boolean supports(ResolutionContext context);
    Map<String, String> provide(ResolutionContext context);
}
