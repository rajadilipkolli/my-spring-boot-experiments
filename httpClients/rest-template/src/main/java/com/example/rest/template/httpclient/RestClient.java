package com.example.rest.template.httpclient;

import com.example.rest.template.model.request.ApplicationRestRequest;
import com.example.rest.template.model.response.ApplicationRestResponse;
import org.springframework.core.ParameterizedTypeReference;

public interface RestClient {

    <T> ApplicationRestResponse<T> get(
            ApplicationRestRequest applicationRestRequest, Class<T> responseType);

    <T> T get(
            ApplicationRestRequest applicationRestRequest,
            ParameterizedTypeReference<T> responseType);

    <T> ApplicationRestResponse<T> post(
            ApplicationRestRequest applicationRestRequest, Class<T> responseType);
}
