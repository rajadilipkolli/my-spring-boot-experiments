package com.example.highrps.shared.idempotency;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import tools.jackson.databind.json.JsonMapper;

@ControllerAdvice
public class IdempotencyResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyResponseBodyAdvice.class);

    private final StringRedisTemplate redisTemplate;
    private final JsonMapper jsonMapper;

    public IdempotencyResponseBodyAdvice(StringRedisTemplate redisTemplate, JsonMapper jsonMapper) {
        this.redisTemplate = redisTemplate;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {

        if (request instanceof ServletServerHttpRequest servletRequest
                && response instanceof ServletServerHttpResponse servletResponse) {
            String method = servletRequest.getMethod().name();
            if (HttpMethod.GET.matches(method) || HttpMethod.OPTIONS.matches(method)) {
                return body;
            }

            String idempotencyKey = servletRequest.getHeaders().getFirst("Idempotency-Key");
            if (idempotencyKey != null && !idempotencyKey.isBlank()) {
                String redisKey = "idempotency:" + idempotencyKey;
                int status = servletResponse.getServletResponse().getStatus();

                if (status >= 200 && status < 300) {
                    try {
                        String jsonBody = body != null ? jsonMapper.writeValueAsString(body) : "";
                        redisTemplate.opsForValue().set(redisKey, jsonBody, Duration.ofHours(24));
                        log.debug("Cached successful response for Idempotency-Key: {}", idempotencyKey);
                    } catch (Exception e) {
                        log.error("Failed to serialize response body for idempotency caching", e);
                        redisTemplate.delete(redisKey);
                    }
                } else if (status >= 400) {
                    // If the request failed, remove the PROCESSING lock so the user can retry safely
                    redisTemplate.delete(redisKey);
                    log.debug("Removed Idempotency-Key lock due to error response: {}", idempotencyKey);
                }
            }
        }
        return body;
    }
}
