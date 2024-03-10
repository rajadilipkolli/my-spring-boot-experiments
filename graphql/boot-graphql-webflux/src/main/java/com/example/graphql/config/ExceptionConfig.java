package com.example.graphql.config;

import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExceptionConfig {

    @Bean
    ErrorProperties errorProperties() {
        final ErrorProperties errorProp = new ErrorProperties();
        errorProp.setIncludeMessage(ErrorProperties.IncludeAttribute.ALWAYS);
        errorProp.setIncludeException(true);
        errorProp.setIncludeBindingErrors(ErrorProperties.IncludeAttribute.ALWAYS);
        return errorProp;
    }
}
