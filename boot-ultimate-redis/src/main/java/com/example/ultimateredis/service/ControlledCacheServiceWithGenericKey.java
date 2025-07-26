package com.example.ultimateredis.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class ControlledCacheServiceWithGenericKey {

    private static final String CONTROLLED_PREFIX = "v2_myControlledPrefix_";

    public static String getCacheKey(String relevant) {
        return CONTROLLED_PREFIX + relevant;
    }

    @Cacheable(
            cacheNames = "myControlledCache",
            key = "T(com.example.ultimateredis.service.ControlledCacheServiceWithGenericKey).getCacheKey(#relevant)")
    public String getFromCache(String relevant) {
        return null;
    }

    @CachePut(
            cacheNames = "myControlledCache",
            key = "T(com.example.ultimateredis.service.ControlledCacheServiceWithGenericKey).getCacheKey(#relevant)")
    public String populateCache(String relevant, String unrelevantTrackingId) {
        return "this is it again!";
    }

    @CacheEvict(
            cacheNames = "myControlledCache",
            key = "T(com.example.ultimateredis.service.ControlledCacheServiceWithGenericKey).getCacheKey(#relevant)")
    public void removeFromCache(String relevant) {}
}
