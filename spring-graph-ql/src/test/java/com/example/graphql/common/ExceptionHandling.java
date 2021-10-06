package com.example.graphql.common;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.zalando.problem.spring.webflux.advice.ProblemHandling;
import org.zalando.problem.spring.webflux.advice.validation.OpenApiValidationAdviceTrait;

import static com.example.graphql.utils.AppConstants.PROFILE_TEST;

@Profile(PROFILE_TEST)
@ControllerAdvice
public final class ExceptionHandling implements ProblemHandling, OpenApiValidationAdviceTrait {

}
