package com.example.ultimateredis.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RedisMetricsAspect {

    private final MeterRegistry meterRegistry;
    private final Map<String, Timer> timerCache = new ConcurrentHashMap<>();

    public RedisMetricsAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Pointcut(
            "execution(* com.example.ultimateredis.service.RedisService.*(..)) || execution(* *..TestRedisService.*(..))")
    public void redisServiceMethods() {}

    @Around("redisServiceMethods()")
    public Object measureRedisOperationTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();

        Timer timer = timerCache.computeIfAbsent(methodName, m -> Timer.builder("redis.operation")
                .tag("method", m)
                .description("Time taken for Redis operations")
                .register(meterRegistry));

        long start = System.nanoTime();
        boolean error = false;
        try {
            return joinPoint.proceed();
        } catch (Exception e) {
            error = true;
            // Count Redis errors
            meterRegistry.counter("redis.errors", "method", methodName).increment();
            throw e;
        } finally {
            long duration = System.nanoTime() - start;

            // Record timing regardless of success/failure
            timer.record(duration, TimeUnit.NANOSECONDS);

            // Count operations by type (success vs failure)
            String outcome = error ? "failure" : "success";
            meterRegistry
                    .counter("redis.operations", "method", methodName, "outcome", outcome)
                    .increment();
        }
    }
}
