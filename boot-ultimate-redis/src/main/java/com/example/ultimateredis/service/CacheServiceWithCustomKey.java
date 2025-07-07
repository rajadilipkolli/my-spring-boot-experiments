package com.example.ultimateredis.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class CacheServiceWithCustomKey {

    private static final Logger log = LoggerFactory.getLogger(CacheServiceWithCustomKey.class);

    @Cacheable(cacheNames = "myCache", key = "'v2_myPrefix_'.concat(#relevant)")
    public String cacheThis(String relevant, String unRelevantTrackingId) {
        log.info("Returning NOT from cache. Tracking: {}!", unRelevantTrackingId);
        return "this Is it";
    }

    @CacheEvict(cacheNames = "myCache", key = "'v2_myPrefix_'.concat(#relevant)")
    public void forgetAboutThis(String relevant) {
        log.info("Forgetting everything about this '{}'!", relevant);
    }
}
