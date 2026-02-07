package com.intend.service;

import com.intend.core.RequestIntent;

import java.util.Map;

public interface IntendService {
    void executeRequest(RequestIntent intent);
    String executeRequestAsString(RequestIntent intent);
    String executeRequestAsString(RequestIntent intent, Map<String, String> captures);
}
