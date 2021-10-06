package com.example.graphql.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.zalando.problem.spring.webflux.advice.ProblemHandling;
import org.zalando.problem.spring.webflux.advice.validation.OpenApiValidationAdviceTrait;

@ControllerAdvice
public final class ExceptionHandling implements ProblemHandling, OpenApiValidationAdviceTrait {
    
}
