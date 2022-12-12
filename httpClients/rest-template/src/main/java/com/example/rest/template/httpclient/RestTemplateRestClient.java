package com.example.rest.template.httpclient;

import com.example.rest.template.model.request.ApplicationRestRequest;
import com.example.rest.template.model.response.ApplicationRestResponse;
import java.net.URI;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class RestTemplateRestClient implements RestClient {

    private final RestTemplate restTemplate;

    @Override
    public <T> ApplicationRestResponse<T> get(
            ApplicationRestRequest applicationRestRequest, Class<T> responseClass) {
        URI uri = getUrlRequestBuilder(applicationRestRequest);
        applicationRestRequest
                .getHeaders()
                .putIfAbsent(HttpHeaders.CONTENT_TYPE, applicationRestRequest.getContentType());
        HttpEntity<Object> httpEntity =
                new HttpEntity<>(
                        applicationRestRequest.getPayload(),
                        convertToMultiMap(applicationRestRequest.getHeaders()));
        return callRestService(uri, HttpMethod.GET, httpEntity, responseClass);
    }

    private URI getUrlRequestBuilder(ApplicationRestRequest applicationRestRequest) {
        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(applicationRestRequest.getHttpBaseUrl());
        if (null != applicationRestRequest.getPath()
                && !"/".equals(applicationRestRequest.getPath())) {
            builder.path(applicationRestRequest.getPath());
        }
        if (!applicationRestRequest.getQueryParameters().isEmpty()) {
            applicationRestRequest.getQueryParameters().forEach(builder::queryParam);
        }
        UriComponents urlComponent;
        if (null != applicationRestRequest.getPathVariables()) {
            urlComponent =
                    builder.buildAndExpand(applicationRestRequest.getPathVariables()).encode();
        } else {
            urlComponent = builder.build(true);
        }

        return urlComponent.toUri();
    }

    private MultiValueMap<String, String> convertToMultiMap(Map<String, String> headers) {
        MultiValueMap<String, String> multiMapValue = new LinkedMultiValueMap<>(headers.size());
        headers.forEach(multiMapValue::add);
        return multiMapValue;
    }

    private <T, V> ApplicationRestResponse<T> callRestService(
            URI uri, HttpMethod httpMethod, HttpEntity<V> httpEntity, Class<T> responseClass) {
        ResponseEntity<T> responseEntity =
                this.restTemplate.exchange(uri, httpMethod, httpEntity, responseClass);
        return getStandardResponse(responseEntity);
    }

    private <T, V> ApplicationRestResponse<T> callRestService(
            URI uri,
            HttpMethod httpMethod,
            HttpEntity<V> httpEntity,
            ParameterizedTypeReference<T> responseType) {
        ResponseEntity<T> responseEntity =
                this.restTemplate.exchange(uri, httpMethod, httpEntity, responseType);
        return getStandardResponse(responseEntity);
    }

    private <T> ApplicationRestResponse<T> getStandardResponse(ResponseEntity<T> responseEntity) {
        return new ApplicationRestResponse<>(
                responseEntity.getBody(),
                responseEntity.getStatusCode().value(),
                responseEntity.getHeaders().toSingleValueMap());
    }
}
