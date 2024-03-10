package com.example.restclient.bootrestclient.config;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration(proxyBeanMethods = false)
@Slf4j
public class RestClientConfiguration {

    @Bean
    RestClient restClient(RestClient.Builder builder, HttpClient jdkClient) {
        String baseUrl = "https://jsonplaceholder.typicode.com";
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(baseUrl);
        return builder.uriBuilderFactory(factory)
                .defaultHeaders(
                        httpHeaders -> {
                            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                            httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
                        })
                .requestFactory(new JdkClientHttpRequestFactory(jdkClient))
                .requestInterceptor(
                        (request, body, execution) -> {
                            // log the http request
                            log.info("URI: {}", request.getURI());
                            log.info("HTTP Method: {}", request.getMethod().name());
                            log.info("HTTP Headers: {}", request.getHeaders());

                            return execution.execute(request, body);
                        })
                .build();
    }

    @Bean
    HttpClient jdkClient() {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(30))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }
}
