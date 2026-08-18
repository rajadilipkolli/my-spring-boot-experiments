package com.example.highrps.shared.idempotency;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class IdempotencyInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyInterceptor.class);
    private final StringRedisTemplate redisTemplate;

    public IdempotencyInterceptor(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (request.getDispatcherType().name().equals("ASYNC")) {
            return true;
        }
        String method = request.getMethod();
        if (HttpMethod.GET.matches(method) || HttpMethod.OPTIONS.matches(method)) {
            return true;
        }

        String idempotencyKey = request.getHeader("Idempotency-Key");
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            response.sendError(
                    HttpStatus.BAD_REQUEST.value(), "Idempotency-Key header is mandatory for mutating requests");
            return false;
        }

        String redisKey = "idempotency:" + idempotencyKey;
        String cachedResponse = redisTemplate.opsForValue().get(redisKey);

        if (cachedResponse != null) {
            if ("PROCESSING".equals(cachedResponse)) {
                response.sendError(
                        HttpStatus.CONFLICT.value(),
                        "A request with this Idempotency-Key is currently being processed.");
                return false;
            }

            log.info("Returning cached idempotent response for key: {}", idempotencyKey);
            response.setStatus(HttpStatus.OK.value());
            response.setContentType("application/json");
            if (!cachedResponse.isEmpty()) {
                response.getWriter().write(cachedResponse);
            }
            return false;
        }

        // Set to PROCESSING to prevent concurrent duplicates
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(redisKey, "PROCESSING", Duration.ofMinutes(5));
        if (Boolean.FALSE.equals(acquired)) {
            response.sendError(
                    HttpStatus.CONFLICT.value(), "A request with this Idempotency-Key is currently being processed.");
            return false;
        }

        return true;
    }
}
