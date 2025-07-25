package com.example.restclient.bootrestclient.config;

import java.util.List;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration(proxyBeanMethods = false)
@EnableRetry
public class RestClientConfiguration {

    @Bean
    RestClient restClient(RestClient.Builder restClientBuilder) {
        return restClientBuilder.build();
    }

    @Bean
    RestClientCustomizer restClientCustomizer(
            ApplicationProperties applicationProperties,
            ClientLoggerRequestInterceptor clientLoggerRequestInterceptor) {
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(applicationProperties.getExternalCallUrl());
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.URI_COMPONENT);
        return restClientBuilder -> restClientBuilder
                .uriBuilderFactory(factory)
                .defaultHeaders(httpHeaders -> {
                    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                    httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
                })
                .requestInterceptor(clientLoggerRequestInterceptor);
    }
}
