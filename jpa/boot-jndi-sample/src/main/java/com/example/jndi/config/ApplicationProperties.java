package com.example.jndi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties("application.datasource")
public class ApplicationProperties {

    private String driverClassName;
    private String url;
    private String username;
    private String password;

    @NestedConfigurationProperty
    private Cors cors = new Cors();

    public static class Cors {
        private String pathPattern = "/api/**";
        private String allowedMethods = "GET, POST, PUT, DELETE";
        private String allowedHeaders = "Content-Type, Accept";
        private String allowedOriginPatterns = "*";
        private boolean allowCredentials = true;
    }
}
