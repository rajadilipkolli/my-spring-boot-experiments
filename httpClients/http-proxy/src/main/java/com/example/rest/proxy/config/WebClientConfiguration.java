package com.example.rest.proxy.config;

import com.example.rest.proxy.client.JsonPlaceholderService;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration(proxyBeanMethods = false)
public class WebClientConfiguration {

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    public HttpServiceProxyFactory httpServiceProxyFactory(WebClient webClient) {
        return HttpServiceProxyFactory.builder(WebClientAdapter.forClient(webClient))
                // default value is 5 sec, as we are unable to get data increasing timeout
                .blockTimeout(Duration.ofSeconds(20))
                .build();
    }

    @Bean
    public JsonPlaceholderService jsonPlaceholderService(
            HttpServiceProxyFactory httpServiceProxyFactory) {
        return httpServiceProxyFactory.createClient(JsonPlaceholderService.class);
    }
}
