package com.example.ultimateredis.config;

import com.example.ultimateredis.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RedisRateLimiter redisRateLimiter;

    public RateLimitInterceptor(RedisRateLimiter redisRateLimiter) {
        this.redisRateLimiter = redisRateLimiter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String clientIp = request.getRemoteAddr();

        // Allow 10 requests per second with burst of 20
        if (!redisRateLimiter.tryAcquire(clientIp, 10.0, 20)) {
            throw new RateLimitExceededException("Too many requests");
        }

        return true;
    }
}
