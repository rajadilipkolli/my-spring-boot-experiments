package com.example.graphql.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.WebExceptionHandler;
import org.zalando.problem.jackson.ProblemModule;
import org.zalando.problem.spring.webflux.advice.ProblemExceptionHandler;
import org.zalando.problem.spring.webflux.advice.ProblemHandling;
import org.zalando.problem.violations.ConstraintViolationProblemModule;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
public class ProblemJacksonAutoConfiguration {

    @Bean
    public ProblemModule problemModule() {
        return new ProblemModule();
    }

    @Bean
    public ConstraintViolationProblemModule constraintViolationProblemModule() {
        return new ConstraintViolationProblemModule();
    }

    @Bean
    @Order(-2)
    // The handler must have precedence over WebFluxResponseStatusExceptionHandler and Spring
    // Boot's ErrorWebExceptionHandler
    public WebExceptionHandler problemExceptionHandler(
        ObjectMapper mapper, ProblemHandling problemHandling) {
        return new ProblemExceptionHandler(mapper, problemHandling);
    }

    @Bean
    public ObjectMapper objectMapper(
        ProblemModule problemModule,
        ConstraintViolationProblemModule constraintViolationProblemModule) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModules(problemModule, constraintViolationProblemModule);
        return objectMapper;
    }
}
