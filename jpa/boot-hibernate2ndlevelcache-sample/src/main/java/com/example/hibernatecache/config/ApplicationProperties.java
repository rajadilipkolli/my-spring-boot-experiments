package com.example.hibernatecache.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties("application")
public class ApplicationProperties {

    @NestedConfigurationProperty private Cors cors = new Cors();

    public static class Cors {
        private String pathPattern = "/api/**";
        private String allowedMethods = "*";
        private String allowedHeaders = "*";
        private String allowedOriginPatterns = "*";
        private boolean allowCredentials = true;

        public String getPathPattern() {
            return pathPattern;
        }

        public Cors setPathPattern(String pathPattern) {
            this.pathPattern = pathPattern;
            return this;
        }

        public String getAllowedMethods() {
            return allowedMethods;
        }

        public Cors setAllowedMethods(String allowedMethods) {
            this.allowedMethods = allowedMethods;
            return this;
        }

        public String getAllowedHeaders() {
            return allowedHeaders;
        }

        public Cors setAllowedHeaders(String allowedHeaders) {
            this.allowedHeaders = allowedHeaders;
            return this;
        }

        public String getAllowedOriginPatterns() {
            return allowedOriginPatterns;
        }

        public Cors setAllowedOriginPatterns(String allowedOriginPatterns) {
            this.allowedOriginPatterns = allowedOriginPatterns;
            return this;
        }

        public boolean isAllowCredentials() {
            return allowCredentials;
        }

        public Cors setAllowCredentials(boolean allowCredentials) {
            this.allowCredentials = allowCredentials;
            return this;
        }
    }

    public Cors getCors() {
        return cors;
    }

    public ApplicationProperties setCors(Cors cors) {
        this.cors = cors;
        return this;
    }
}
