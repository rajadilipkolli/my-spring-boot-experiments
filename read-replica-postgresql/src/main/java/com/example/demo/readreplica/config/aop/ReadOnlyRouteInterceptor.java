package com.example.demo.readreplica.config.aop;

import com.example.demo.readreplica.config.routing.RoutingDataSource;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Aspect
@Component
@Order(0)
public class ReadOnlyRouteInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReadOnlyRouteInterceptor.class);

    @Around("@annotation(transactional)")
    public Object proceed(ProceedingJoinPoint proceedingJoinPoint, Transactional transactional) throws Throwable {
        try {
            if (transactional.readOnly()) {
                RoutingDataSource.setReplicaRoute();
                LOGGER.info("Routing database call to the read replica");
            }
            return proceedingJoinPoint.proceed();
        } finally {
            RoutingDataSource.clearReplicaRoute();
        }
    }
}
