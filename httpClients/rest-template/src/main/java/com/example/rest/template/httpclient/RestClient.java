package com.example.rest.template.httpclient;

import com.example.rest.template.model.request.ApplicationRestRequest;
import com.example.rest.template.model.response.ApplicationRestResponse;

public interface RestClient {

    <T> ApplicationRestResponse<T> get(
            ApplicationRestRequest applicationRestRequest, Class<T> responseClass);
}
