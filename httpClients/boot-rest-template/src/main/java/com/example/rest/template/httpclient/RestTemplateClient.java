package com.example.rest.template.httpclient;

import com.example.rest.template.model.request.ApplicationRestRequest;
import com.example.rest.template.model.response.ApplicationRestResponse;
import org.springframework.core.ParameterizedTypeReference;

public interface RestTemplateClient {

    <T> ApplicationRestResponse<T> get(ApplicationRestRequest applicationRestRequest, Class<T> responseType);

    <T> T get(ApplicationRestRequest applicationRestRequest, ParameterizedTypeReference<T> responseType);

    <T> ApplicationRestResponse<T> post(ApplicationRestRequest applicationRestRequest, Class<T> responseType);

    <T> ApplicationRestResponse<T> put(ApplicationRestRequest applicationRestRequest, Class<T> responseClass);

    <T> ApplicationRestResponse<T> patch(ApplicationRestRequest applicationRestRequest, Class<T> responseClass);

    <T> ApplicationRestResponse<T> delete(ApplicationRestRequest applicationRestRequest, Class<T> responseClass);
}
