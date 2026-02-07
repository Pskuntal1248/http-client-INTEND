package com.intend.repository;

public interface StateRepository {
    String getLastIdempotencyKey(String key);
    void saveIdempotencyKey(String key, String uuid);
}
