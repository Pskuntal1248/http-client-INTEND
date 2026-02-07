package com.intend.repository;

import com.intend.context.ResolutionContext;
import com.intend.core.RequestIntent;

public interface ContextRepository {
    /**
     * Loads the environment secrets and config required for this intent.
     */
    ResolutionContext loadContext(RequestIntent intent);
}
