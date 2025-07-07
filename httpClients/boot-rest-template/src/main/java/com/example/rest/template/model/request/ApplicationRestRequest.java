package com.example.rest.template.model.request;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.MediaType;

public class ApplicationRestRequest {

    private String path;
    private String httpBaseUrl;
    private Map<String, String> queryParameters = new HashMap<>();
    private Map<String, Object> pathVariables;
    private Map<String, String> headers = new HashMap<>();
    private Object payload;
    private String contentType = MediaType.APPLICATION_JSON_VALUE;

    public ApplicationRestRequest() {}

    public String getPath() {
        return path;
    }

    public String getHttpBaseUrl() {
        return httpBaseUrl;
    }

    public void setHttpBaseUrl(String httpBaseUrl) {
        this.httpBaseUrl = httpBaseUrl;
    }

    public Map<String, String> getQueryParameters() {
        return queryParameters;
    }

    public Map<String, Object> getPathVariables() {
        return pathVariables;
    }

    public void setPathVariables(Map<String, Object> pathVariables) {
        this.pathVariables = pathVariables;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public String getContentType() {
        return contentType;
    }

    private ApplicationRestRequest(Builder builder) {
        this.path = builder.path;
        this.httpBaseUrl = builder.httpBaseUrl;
        this.queryParameters = builder.queryParameters;
        this.pathVariables = builder.pathVariables;
        this.headers = builder.headers;
        this.payload = builder.payload;
        this.contentType = builder.contentType;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String path;
        private String httpBaseUrl;
        private Map<String, String> queryParameters = new HashMap<>();
        private Map<String, Object> pathVariables;
        private Map<String, String> headers = new HashMap<>();
        private Object payload;
        private String contentType = MediaType.APPLICATION_JSON_VALUE;

        public Builder path(String path) {
            this.path = path != null && !path.startsWith("/") ? "/" + path : path;
            return this;
        }

        public Builder httpBaseUrl(String httpBaseUrl) {
            this.httpBaseUrl = httpBaseUrl;
            return this;
        }

        public Builder queryParameters(Map<String, String> queryParameters) {
            if (queryParameters != null) {
                this.queryParameters = new HashMap<>(queryParameters);
            }
            return this;
        }

        public Builder pathVariables(Map<String, Object> pathVariables) {
            this.pathVariables = pathVariables;
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            if (headers != null) {
                this.headers = new HashMap<>(headers);
            }
            return this;
        }

        public Builder payload(Object payload) {
            this.payload = payload;
            return this;
        }

        public Builder contentType(String contentType) {
            if (contentType != null) {
                this.contentType = contentType;
            }
            return this;
        }

        public ApplicationRestRequest build() {
            return new ApplicationRestRequest(this);
        }
    }

    public void setPath(final String path) {
        this.path = path.startsWith("/") ? path : "/" + path;
    }

    public void setQueryParameters(final Map<String, String> queryParameters) {
        if (null != queryParameters) {
            this.queryParameters = new HashMap<>(queryParameters);
        }
    }

    public void setHeaders(final Map<String, String> headers) {
        if (null != headers) {
            this.headers = new HashMap<>(headers);
        }
    }

    public void setContentType(final String contentType) {
        if (null != contentType) {
            this.contentType = contentType;
        }
    }
}
