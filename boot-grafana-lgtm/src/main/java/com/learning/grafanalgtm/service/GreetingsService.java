package com.learning.grafanalgtm.service;

import io.micrometer.observation.annotation.Observed;
import io.micrometer.tracing.annotation.SpanTag;
import org.springframework.stereotype.Service;

@Service
public class GreetingsService {

    @Observed(name = "greetingByUserServiceObservation", contextualName = "greetingByUser")
    public String greetingByUser(@SpanTag("userName") String userName) {
        return "Hello, " + userName + "!";
    }
}
