package com.example.rest.template.httpclient;

import com.example.rest.template.model.request.ApplicationRestRequest;
import com.example.rest.template.model.response.ApplicationRestResponse;
import java.net.URI;
import java.util.Map;
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
public class RestTemplateClientImpl implements RestTemplateClient {

    private final RestTemplate restTemplate;

    @Override
    public <T> ApplicationRestResponse<T> get(
            ApplicationRestRequest applicationRestRequest, Class<T> responseType) {
        return getApplicationRestResponse(applicationRestRequest, responseType, HttpMethod.GET);
    }

    @Override
    public <T> T get(
            ApplicationRestRequest applicationRestRequest,
            ParameterizedTypeReference<T> responseType) {
        UrlAndHttpEntityRecord urlAndHttpEntityRecord = getUrlAndHttpEntity(applicationRestRequest);
        return callRestService(
                        urlAndHttpEntityRecord.uri(),
                        HttpMethod.GET,
                        urlAndHttpEntityRecord.httpEntity(),
                        responseType)
                .body();
    }

    @Override
    public <T> ApplicationRestResponse<T> post(
            ApplicationRestRequest applicationRestRequest, Class<T> responseType) {
        return getApplicationRestResponse(applicationRestRequest, responseType, HttpMethod.POST);
    }

    @Override
    public <T> ApplicationRestResponse<T> put(
            ApplicationRestRequest applicationRestRequest, Class<T> responseType) {
        return getApplicationRestResponse(applicationRestRequest, responseType, HttpMethod.PUT);
    }

    @Override
    public <T> ApplicationRestResponse<T> patch(
            ApplicationRestRequest applicationRestRequest, Class<T> responseType) {
        return getApplicationRestResponse(applicationRestRequest, responseType, HttpMethod.PATCH);
    }

    @Override
    public <T> ApplicationRestResponse<T> delete(
            ApplicationRestRequest applicationRestRequest, Class<T> responseType) {
        return getApplicationRestResponse(applicationRestRequest, responseType, HttpMethod.DELETE);
    }

    private <T> ApplicationRestResponse<T> getApplicationRestResponse(
            ApplicationRestRequest applicationRestRequest,
            Class<T> responseType,
            HttpMethod httpMethod) {
        UrlAndHttpEntityRecord urlAndHttpEntityRecord = getUrlAndHttpEntity(applicationRestRequest);
        return callRestService(
                urlAndHttpEntityRecord.uri(),
                httpMethod,
                urlAndHttpEntityRecord.httpEntity(),
                responseType);
    }

    private UrlAndHttpEntityRecord getUrlAndHttpEntity(
            ApplicationRestRequest applicationRestRequest) {

        URI uri = getUrlRequestBuilder(applicationRestRequest);
        applicationRestRequest
                .getHeaders()
                .putIfAbsent(HttpHeaders.CONTENT_TYPE, applicationRestRequest.getContentType());
        HttpEntity<Object> httpEntity =
                new HttpEntity<>(
                        applicationRestRequest.getPayload(),
                        convertToMultiMap(applicationRestRequest.getHeaders()));

        return new UrlAndHttpEntityRecord(uri, httpEntity);
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

    private record UrlAndHttpEntityRecord(URI uri, HttpEntity<Object> httpEntity) {}
}
