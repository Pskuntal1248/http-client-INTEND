package com.intend.service;

import com.intend.core.RequestIntent;
import java.util.Map;

public interface IntendService {
    Map<String, String> prepareRequest(RequestIntent intent);
}
