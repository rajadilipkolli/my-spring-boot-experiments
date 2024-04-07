package com.example.restclient.bootrestclient.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties("application")
public class ApplicationProperties {

    @NotBlank(message = "External Call URL cant be Blank")
    private String externalCallUrl;

    @NestedConfigurationProperty private Cors cors = new Cors();

    @Getter
    @Setter
    public static class Cors {
        private String pathPattern = "/api/**";
        private String allowedMethods = "*";
        private String allowedHeaders = "*";
        private String allowedOriginPatterns = "*";
        private boolean allowCredentials = true;
    }
}
