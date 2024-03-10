package com.example.rest.proxy.config;

import com.example.rest.proxy.client.JsonPlaceholderService;
import io.micrometer.observation.ObservationRegistry;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
@Slf4j
public class RestClientConfiguration {

    private final ApplicationProperties applicationProperties;

    @Bean
    HttpServiceProxyFactory httpServiceProxyFactory(
            RestClient.Builder builder, ObservationRegistry observationRegistry) {
        RestClient restClient =
                builder.defaultHeaders(
                                httpHeaders -> {
                                    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                                    httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
                                })
                        .baseUrl(applicationProperties.getPostServiceUrl())
                        .observationRegistry(observationRegistry)
                        .requestInterceptor(
                                (request, body, execution) -> {
                                    // log the http request
                                    log.info("URI: {}", request.getURI());
                                    log.info("HTTP Method: {}", request.getMethod().name());
                                    log.info("HTTP Headers: {}", request.getHeaders());

                                    return execution.execute(request, body);
                                })
                        .build();
        RestClientAdapter webClientAdapter = RestClientAdapter.create(restClient);
        return HttpServiceProxyFactory.builderFor(webClientAdapter).build();
    }

    @Bean
    JsonPlaceholderService jsonPlaceholderService(HttpServiceProxyFactory httpServiceProxyFactory) {
        return httpServiceProxyFactory.createClient(JsonPlaceholderService.class);
    }
}
