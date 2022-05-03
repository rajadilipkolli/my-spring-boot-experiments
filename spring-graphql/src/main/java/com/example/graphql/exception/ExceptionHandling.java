package com.example.graphql.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.zalando.problem.spring.webflux.advice.ProblemHandling;

@ControllerAdvice
public final class ExceptionHandling implements ProblemHandling {}
