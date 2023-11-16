package com.example.rest.proxy.config;

import com.example.rest.proxy.client.JsonPlaceholderService;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class RestClientConfiguration {

    private final ApplicationProperties applicationProperties;

    @Bean
    HttpServiceProxyFactory httpServiceProxyFactory(
            RestClient.Builder builder, ObservationRegistry observationRegistry) {
        RestClient restClient =
                builder.defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .baseUrl(applicationProperties.getPostServiceUrl())
                        .observationRegistry(observationRegistry)
                        .build();
        RestClientAdapter webClientAdapter = RestClientAdapter.create(restClient);
        return HttpServiceProxyFactory.builderFor(webClientAdapter).build();
    }

    @Bean
    JsonPlaceholderService jsonPlaceholderService(HttpServiceProxyFactory httpServiceProxyFactory) {
        return httpServiceProxyFactory.createClient(JsonPlaceholderService.class);
    }
}
