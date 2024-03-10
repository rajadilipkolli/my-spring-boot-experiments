package com.example.rest.template.config;

import static com.example.rest.template.utils.AppConstants.*;

import java.time.Duration;
import java.util.Iterator;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.ConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.client.RestTemplate;

@Configuration(proxyBeanMethods = false)
@EnableScheduling
@Slf4j
public class RestTemplateConfiguration {

    @Bean
    PoolingHttpClientConnectionManager poolingConnectionManager() {
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

        // set a total amount of connections across all HTTP routes
        poolingConnectionManager.setMaxTotal(MAX_TOTAL_CONNECTIONS);
        // set a maximum amount of connections for each HTTP route in pool
        poolingConnectionManager.setDefaultMaxPerRoute(MAX_ROUTE_CONNECTIONS);
        // increase the amounts of connections if the host is localhost
        HttpHost localhost = new HttpHost("http://localhost", 8080);
        poolingConnectionManager.setMaxPerRoute(
                new HttpRoute(localhost), MAX_LOCALHOST_CONNECTIONS);
        return poolingConnectionManager;
    }

    @Bean
    CloseableHttpClient httpClient(
            PoolingHttpClientConnectionManager poolingConnectionManager,
            ConnectionKeepAliveStrategy connectionKeepAliveStrategy) {
        RequestConfig requestConfig =
                RequestConfig.custom()
                        .setConnectionKeepAlive(TimeValue.ofSeconds(CONNECTION_TIMEOUT))
                        .setConnectionRequestTimeout(Timeout.ofSeconds(REQUEST_TIMEOUT))
                        .setResponseTimeout(Timeout.ofSeconds(SOCKET_TIMEOUT))
                        .build();

        return HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(poolingConnectionManager)
                .setKeepAliveStrategy(connectionKeepAliveStrategy)
                .setConnectionManagerShared(true)
                .build();
    }

    @Bean
    ConnectionKeepAliveStrategy connectionKeepAliveStrategy() {
        return (httpResponse, httpContext) -> {
            Iterator<Header> headerIterator = httpResponse.headerIterator(HttpHeaders.KEEP_ALIVE);
            while (headerIterator.hasNext()) {
                Header element = headerIterator.next();
                String param = element.getName();
                String value = element.getValue();
                if (value != null && param.equalsIgnoreCase(HttpHeaders.TIMEOUT)) {
                    return TimeValue.ofSeconds(Long.parseLong(value));
                }
            }

            return TimeValue.ofSeconds(DEFAULT_KEEP_ALIVE_TIME);
        };
    }

    @Bean
    RestTemplate restTemplate(
            RestTemplateBuilder restTemplateBuilder, CloseableHttpClient httpClient) {

        return restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(60))
                .defaultHeader(
                        org.springframework.http.HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON_VALUE)
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory(httpClient))
                .interceptors(
                        ((request, body, execution) -> {
                            // log the http request
                            log.info("URI: {}", request.getURI());
                            log.info("HTTP Method: {}", request.getMethod().name());
                            log.info("HTTP Headers: {}", request.getHeaders());

                            return execution.execute(request, body);
                        }))
                .build();
    }

    // close idleConnections
    @Bean
    Runnable idleConnectionMonitor(PoolingHttpClientConnectionManager pool) {
        return new Runnable() {
            @Override
            @Scheduled(fixedDelay = 20000)
            public void run() {
                // only if connection pool is initialised
                if (pool != null) {
                    pool.closeExpired();
                    pool.closeIdle(TimeValue.ofSeconds(IDLE_CONNECTION_WAIT_TIME));
                    log.info("Idle connection monitor: Closing expired and idle connections");
                }
            }
        };
    }

    // Required for Scheduler
    @Bean
    TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("idleMonitor");
        scheduler.setPoolSize(5);
        return scheduler;
    }
}
