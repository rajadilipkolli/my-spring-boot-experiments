package com.example.rest.template.model.request;

import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.MediaType;

@Getter
@Setter
@Builder
public class ApplicationRestRequest {

    private String path;

    private String httpBaseUrl;

    @Builder.Default private Map<String, String> queryParameters = new HashMap<>();

    private Map<String, String> pathVariables;

    @Builder.Default private Map<String, String> headers = new HashMap<>();
    private Object payload;

    @Builder.Default private String contentType = MediaType.APPLICATION_JSON_VALUE;

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
