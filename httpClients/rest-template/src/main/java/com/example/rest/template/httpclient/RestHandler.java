package com.example.rest.template.httpclient;

import com.example.rest.template.model.request.ApplicationRestRequest;
import com.example.rest.template.model.response.ApplicationRestResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RestHandler {

    private final RestClient restClient;

    public ApplicationRestResponse<String> get(ApplicationRestRequest applicationRestRequest) {

        return get(applicationRestRequest, String.class);
    }

    public String getBody(ApplicationRestRequest applicationRestRequest) {

        return get(applicationRestRequest, String.class).body();
    }

    public <T> ApplicationRestResponse<T> get(
            ApplicationRestRequest applicationRestRequest, Class<T> responseClass) {

        return this.restClient.get(applicationRestRequest, responseClass);
    }

    public <T> T getBody(ApplicationRestRequest applicationRestRequest, Class<T> responseClass) {

        return get(applicationRestRequest, responseClass).body();
    }
}
