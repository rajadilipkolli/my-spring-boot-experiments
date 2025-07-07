package com.example.rest.template.httpclient;

import com.example.rest.template.model.request.ApplicationRestRequest;
import com.example.rest.template.model.response.ApplicationRestResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

@Service
public class RestHandler {

    private final RestTemplateClient restTemplateClient;

    public ApplicationRestResponse<String> get(ApplicationRestRequest applicationRestRequest) {

        return get(applicationRestRequest, String.class);
    }

    public String getBody(ApplicationRestRequest applicationRestRequest) {

        return get(applicationRestRequest, String.class).body();
    }

    public <T> ApplicationRestResponse<T> get(
            ApplicationRestRequest applicationRestRequest, Class<T> responseClass) {

        return this.restTemplateClient.get(applicationRestRequest, responseClass);
    }

    public <T> T getBody(ApplicationRestRequest applicationRestRequest, Class<T> responseClass) {

        return get(applicationRestRequest, responseClass).body();
    }

    public <T> T getBody(
            ApplicationRestRequest applicationRestRequest,
            ParameterizedTypeReference<T> responseType) {
        return this.restTemplateClient.get(applicationRestRequest, responseType);
    }

    public ApplicationRestResponse<String> post(ApplicationRestRequest applicationRestRequest) {

        return post(applicationRestRequest, String.class);
    }

    public <T> ApplicationRestResponse<T> post(
            ApplicationRestRequest applicationRestRequest, Class<T> responseClass) {

        return this.restTemplateClient.post(applicationRestRequest, responseClass);
    }

    public ApplicationRestResponse<String> put(ApplicationRestRequest applicationRestRequest) {

        return put(applicationRestRequest, String.class);
    }

    public <T> ApplicationRestResponse<T> put(
            ApplicationRestRequest applicationRestRequest, Class<T> responseClass) {

        return this.restTemplateClient.put(applicationRestRequest, responseClass);
    }

    public <T> ApplicationRestResponse<T> patch(
            ApplicationRestRequest applicationRestRequest, Class<T> responseClass) {

        return this.restTemplateClient.patch(applicationRestRequest, responseClass);
    }

    public <T> ApplicationRestResponse<T> delete(
            ApplicationRestRequest applicationRestRequest, Class<T> responseClass) {

        return this.restTemplateClient.delete(applicationRestRequest, responseClass);
    }
}
