package com.example.restclient.bootrestclient.services;

import com.example.restclient.bootrestclient.exception.MyCustomClientException;
import java.net.URI;
import java.util.Map;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

@Service
@Retryable(
        includes = {HttpServerErrorException.class},
        maxAttempts = 2,
        jitter = 5000L,
        multiplier = 2)
public class HttpClientService {

    private final RestClient restClient;

    public HttpClientService(RestClient restClient) {
        this.restClient = restClient;
    }

    <T> T callAndFetchResponseForGetMethod(
            Function<UriBuilder, URI> uriFunction, @Nullable Map<String, String> headers, Class<T> bodyType) {
        return callServer(uriFunction, HttpMethod.GET, headers, null, bodyType, null);
    }

    <T> T callAndFetchResponseForGetMethod(
            Function<UriBuilder, URI> uriFunction, ParameterizedTypeReference<T> bodyType) {
        return callServer(uriFunction, HttpMethod.GET, null, null, null, bodyType);
    }

    <T> T callAndFetchResponseForPostMethod(Function<UriBuilder, URI> uriFunction, Object body, Class<T> bodyType) {
        return callServer(uriFunction, HttpMethod.POST, null, body, bodyType, null);
    }

    public <T> T callAndFetchResponseForPutMethod(
            Function<UriBuilder, URI> uriFunction, Object body, Class<T> bodyType) {
        return callServer(uriFunction, HttpMethod.PUT, null, body, bodyType, null);
    }

    String callAndFetchResponseForDeleteMethod(Function<UriBuilder, URI> uriFunction) {
        return callAndFetchResponseForDeleteMethod(uriFunction, String.class);
    }

    <T> T callAndFetchResponseForDeleteMethod(Function<UriBuilder, URI> uriFunction, Class<T> bodyType) {
        return callServer(uriFunction, HttpMethod.DELETE, null, null, bodyType, null);
    }

    private <T> T callServer(
            Function<UriBuilder, URI> uriFunction,
            HttpMethod httpMethod,
            Map<String, String> headers,
            Object body,
            Class<T> bodyType,
            ParameterizedTypeReference<T> typeReferenceBodyType) {
        RestClient.RequestBodySpec uri = restClient.method(httpMethod).uri(uriFunction);
        if (!CollectionUtils.isEmpty(headers)) {
            uri.headers(httpHeader -> headers.keySet().forEach(key -> httpHeader.add(key, headers.get(key))));
        }
        if (body != null) {
            uri.body(body);
        }
        RestClient.ResponseSpec responseSpec = uri.retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new MyCustomClientException(response.getStatusCode(), response.getHeaders());
                });
        if (bodyType != null) {
            return responseSpec.body(bodyType);
        } else {
            return responseSpec.body(typeReferenceBodyType);
        }
    }
}
