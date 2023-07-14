package com.example.ultimateredis.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CacheServiceWithCustomKey {

    @Cacheable(cacheNames = "myCache", key = "'myPrefix_'.concat(#relevant)")
    public String cacheThis(String relevant, String unRelevantTrackingId) {
        log.info("Returning NOT from cache. Tracking: {}!", unRelevantTrackingId);
        return "this Is it";
    }

    @CacheEvict(cacheNames = "myCache", key = "'myPrefix_'.concat(#relevant)")
    public void forgetAboutThis(String relevant) {
        log.info("Forgetting everything about this '{}'!", relevant);
    }
}
