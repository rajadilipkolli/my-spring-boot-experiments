package com.example.rest.webclient.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.webclient.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.util.retry.Retry;

@Configuration(proxyBeanMethods = false)
class WebClientConfiguration {

    @Bean
    WebClient webClient(WebClient.Builder builder) {
        return builder.baseUrl("https://jsonplaceholder.typicode.com").build();
    }

    @Bean
    WebClientCustomizer webClientCustomizer() {
        ConnectionProvider connectionProvider =
                ConnectionProvider.builder("custom")
                        .maxConnections(100)
                        .pendingAcquireMaxCount(500)
                        .maxIdleTime(Duration.ofSeconds(20))
                        .maxLifeTime(Duration.ofSeconds(60))
                        .build();

        HttpClient httpClient =
                HttpClient.create(connectionProvider)
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                        .responseTimeout(Duration.ofSeconds(5))
                        .doOnConnected(
                                conn ->
                                        conn.addHandlerLast(
                                                        new ReadTimeoutHandler(5, TimeUnit.SECONDS))
                                                .addHandlerLast(
                                                        new WriteTimeoutHandler(
                                                                5, TimeUnit.SECONDS)));

        return webClientBuilder ->
                webClientBuilder
                        .defaultHeader(HttpHeaders.ACCEPT_ENCODING, "gzip")
                        .filter(
                                (request, next) ->
                                        next.exchange(request)
                                                .retryWhen(
                                                        Retry.backoff(3, Duration.ofMillis(100))))
                        .clientConnector(new ReactorClientHttpConnector(httpClient));
    }
}
