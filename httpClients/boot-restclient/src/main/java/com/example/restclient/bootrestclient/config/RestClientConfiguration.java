package com.example.restclient.bootrestclient.config;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.lang.NonNull;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration(proxyBeanMethods = false)
@Slf4j
public class RestClientConfiguration {

    @Bean
    RestClient restClient(
            RestClient.Builder builder,
            @NonNull BufferingClientHttpRequestFactory bufferingClientHttpRequestFactory) {
        String baseUrl = "https://jsonplaceholder.typicode.com";
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(baseUrl);
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.URI_COMPONENT);
        return builder.uriBuilderFactory(factory)
                .defaultHeaders(
                        httpHeaders -> {
                            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                            httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
                        })
                .requestFactory(bufferingClientHttpRequestFactory)
                .requestInterceptor(
                        (request, body, execution) -> {
                            logRequest(request, body);
                            ClientHttpResponse response = execution.execute(request, body);
                            logResponse(response);
                            return response;
                        })
                .build();
    }

    private void logResponse(ClientHttpResponse response) throws IOException {
        log.info(
                "============================response begin==========================================");
        log.info("Status code  : {}", response.getStatusCode());
        log.info("Status text  : {}", response.getStatusText());
        log.info("Headers      : {}", response.getHeaders());
        log.info(
                "Response body: {}",
                StreamUtils.copyToString(response.getBody(), Charset.defaultCharset()));
        log.info(
                "=======================response end=================================================");
    }

    private void logRequest(HttpRequest request, byte[] body) {

        log.info(
                "===========================request begin================================================");
        log.info("URI         : {}", request.getURI());
        log.info("Method      : {}", request.getMethod());
        log.info("Headers     : {}", request.getHeaders());
        log.info("Request body: {}", new String(body, StandardCharsets.UTF_8));
        log.info(
                "==========================request end================================================");
    }

    @Bean
    HttpClient jdkClient() {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(30))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @Bean
    JdkClientHttpRequestFactory jdkClientHttpRequestFactory(@NonNull HttpClient jdkClient) {
        return new JdkClientHttpRequestFactory(jdkClient);
    }

    // BufferingClientHttpRequestFactory allows us to read the response body multiple times for a
    // single request.
    @Bean
    BufferingClientHttpRequestFactory bufferingClientHttpRequestFactory(
            @NonNull JdkClientHttpRequestFactory jdkClientHttpRequestFactory) {
        return new BufferingClientHttpRequestFactory(jdkClientHttpRequestFactory);
    }
}
