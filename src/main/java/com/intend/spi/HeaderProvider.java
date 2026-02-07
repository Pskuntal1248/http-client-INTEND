package com.intend.spi;

import com.intend.context.ResolutionContext;

public interface HeaderProvider {
    int getOrder();
    boolean supports(ResolutionContext context);
    HeaderResolution resolve(ResolutionContext context);
}
