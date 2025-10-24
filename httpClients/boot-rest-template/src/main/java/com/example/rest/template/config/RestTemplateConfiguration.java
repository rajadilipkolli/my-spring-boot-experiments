package com.example.rest.template.config;

import static com.example.rest.template.utils.AppConstants.CONNECTION_TIMEOUT;
import static com.example.rest.template.utils.AppConstants.DEFAULT_KEEP_ALIVE_TIME;
import static com.example.rest.template.utils.AppConstants.IDLE_CONNECTION_WAIT_TIME;
import static com.example.rest.template.utils.AppConstants.MAX_LOCALHOST_CONNECTIONS;
import static com.example.rest.template.utils.AppConstants.MAX_ROUTE_CONNECTIONS;
import static com.example.rest.template.utils.AppConstants.MAX_TOTAL_CONNECTIONS;
import static com.example.rest.template.utils.AppConstants.REQUEST_TIMEOUT;
import static com.example.rest.template.utils.AppConstants.SOCKET_TIMEOUT;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import javax.net.ssl.SSLContext;
import org.apache.hc.client5.http.ConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.HostnameVerificationPolicy;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.client.RestTemplate;

@Configuration(proxyBeanMethods = false)
@EnableScheduling
public class RestTemplateConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RestTemplateConfiguration.class);

    private final Environment environment;

    public RestTemplateConfiguration(Environment environment) {
        this.environment = environment;
    }

    @Bean
    PoolingHttpClientConnectionManager poolingConnectionManager() {
        SSLContext sslContext;
        try {
            // Configure SSLContext with a permissive TrustStrategy
            SSLContextBuilder sslContextBuilder = SSLContextBuilder.create();
            if (!isProdEnvironment()) {
                log.warn("Using permissive certificate validation - NOT FOR PRODUCTION USE");
                sslContextBuilder.loadTrustMaterial((chain, authType) -> true);
            }
            sslContext = sslContextBuilder.build();
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new RuntimeException("Failed to initialize SSL context", e);
        }
        PoolingHttpClientConnectionManager poolingConnectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setTlsSocketStrategy(new DefaultClientTlsStrategy(
                        sslContext,
                        HostnameVerificationPolicy.CLIENT,
                        isProdEnvironment() ? null : NoopHostnameVerifier.INSTANCE))
                .setDefaultSocketConfig(SocketConfig.custom()
                        .setSoTimeout(Timeout.ofSeconds(SOCKET_TIMEOUT))
                        .build())
                .setDefaultConnectionConfig(ConnectionConfig.custom()
                        .setConnectTimeout(Timeout.ofSeconds(CONNECTION_TIMEOUT))
                        .build())
                // set a total amount of connections across all HTTP routes
                .setMaxConnTotal(MAX_TOTAL_CONNECTIONS)
                // set a maximum amount of connections for each HTTP route in pool
                .setMaxConnPerRoute(MAX_ROUTE_CONNECTIONS)
                .build();

        // increase the amounts of connections if the host is localhost
        HttpHost localhost = new HttpHost("http://localhost", 8080);
        poolingConnectionManager.setMaxPerRoute(new HttpRoute(localhost), MAX_LOCALHOST_CONNECTIONS);
        return poolingConnectionManager;
    }

    private boolean isProdEnvironment() {
        return List.of(environment.getActiveProfiles()).contains("prod");
    }

    @Bean
    CloseableHttpClient httpClient(
            PoolingHttpClientConnectionManager poolingConnectionManager,
            ConnectionKeepAliveStrategy connectionKeepAliveStrategy) {
        RequestConfig requestConfig = RequestConfig.custom()
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
            Iterator<Header> headerIterator = httpResponse.headerIterator("keep-alive");
            while (headerIterator.hasNext()) {
                Header element = headerIterator.next();
                String param = element.getName();
                String value = element.getValue();
                if (value != null && param.equalsIgnoreCase("timeout")) {
                    return TimeValue.ofSeconds(Long.parseLong(value));
                }
            }

            return TimeValue.ofSeconds(DEFAULT_KEEP_ALIVE_TIME);
        };
    }

    @Bean
    RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder, CloseableHttpClient httpClient) {

        return restTemplateBuilder
                .connectTimeout(Duration.ofSeconds(60))
                .defaultHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory(httpClient))
                .interceptors(((request, body, execution) -> {
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
