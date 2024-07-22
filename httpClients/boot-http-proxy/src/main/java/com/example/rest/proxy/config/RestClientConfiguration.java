package com.example.rest.proxy.config;

import com.example.rest.proxy.client.JsonPlaceholderService;
import io.micrometer.observation.ObservationRegistry;
import java.util.List;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.zalando.logbook.spring.LogbookClientHttpRequestInterceptor;

@Configuration(proxyBeanMethods = false)
class RestClientConfiguration {

    private final ApplicationProperties applicationProperties;

    RestClientConfiguration(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @Bean
    public CloseableHttpClient httpClient() {
        Registry<ConnectionSocketFactory> registry =
                RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", PlainConnectionSocketFactory.getSocketFactory())
                        .register("https", SSLConnectionSocketFactory.getSocketFactory())
                        .build();
        PoolingHttpClientConnectionManager poolingConnectionManager =
                new PoolingHttpClientConnectionManager(registry);

        poolingConnectionManager.setDefaultSocketConfig(
                SocketConfig.custom().setSoTimeout(Timeout.ofSeconds(2)).build());
        poolingConnectionManager.setDefaultConnectionConfig(
                ConnectionConfig.custom().setConnectTimeout(Timeout.ofSeconds(2)).build());

        // set total amount of connections across all HTTP routes
        poolingConnectionManager.setMaxTotal(200);
        // set maximum amount of connections for each http route in pool
        poolingConnectionManager.setDefaultMaxPerRoute(100);

        RequestConfig requestConfig =
                RequestConfig.custom()
                        .setConnectionKeepAlive(TimeValue.ofSeconds(10))
                        .setConnectionRequestTimeout(Timeout.ofSeconds(2))
                        .setResponseTimeout(Timeout.ofSeconds(2))
                        .build();

        return HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(poolingConnectionManager)
                .setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())
                .build();
    }

    @Bean
    RestClientCustomizer restClientCustomizer(
            ObservationRegistry observationRegistry,
            CloseableHttpClient httpClient,
            LogbookClientHttpRequestInterceptor interceptor) {
        return restClientBuilder ->
                restClientBuilder
                        .defaultHeaders(
                                httpHeaders -> {
                                    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                                    httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
                                })
                        .baseUrl(applicationProperties.getPostServiceUrl())
                        .requestFactory(new HttpComponentsClientHttpRequestFactory(httpClient))
                        .observationRegistry(observationRegistry)
                        .requestInterceptor(interceptor);
    }

    @Bean
    HttpServiceProxyFactory httpServiceProxyFactory(RestClient.Builder restClientBuilder) {
        RestClientAdapter webClientAdapter = RestClientAdapter.create(restClientBuilder.build());
        return HttpServiceProxyFactory.builderFor(webClientAdapter).build();
    }

    @Bean
    JsonPlaceholderService jsonPlaceholderService(HttpServiceProxyFactory httpServiceProxyFactory) {
        return httpServiceProxyFactory.createClient(JsonPlaceholderService.class);
    }
}
