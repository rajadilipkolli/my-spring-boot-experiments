package com.example.rest.proxy.config;

import com.example.rest.proxy.client.JsonPlaceholderService;
import io.micrometer.observation.ObservationRegistry;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import javax.net.ssl.SSLContext;
import org.apache.hc.client5.http.impl.DefaultConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.HostnameVerificationPolicy;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpComponentsClientHttpRequestFactoryBuilder;
import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration(proxyBeanMethods = false)
class RestClientConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RestClientConfiguration.class);

    private final ApplicationProperties applicationProperties;
    private final Environment environment;

    RestClientConfiguration(ApplicationProperties applicationProperties, Environment environment) {
        this.applicationProperties = applicationProperties;
        this.environment = environment;
    }

    @Bean
    HttpComponentsClientHttpRequestFactoryBuilder httpComponentsClientHttpRequestFactoryBuilder() {

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

        return ClientHttpRequestFactoryBuilder.httpComponents()
                .withHttpClientCustomizer(httpClientBuilder ->
                        httpClientBuilder.setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy.INSTANCE))
                .withConnectionManagerCustomizer(poolingHttpClientConnectionManagerBuilder -> {
                    poolingHttpClientConnectionManagerBuilder.setMaxConnTotal(200);
                    poolingHttpClientConnectionManagerBuilder.setMaxConnPerRoute(100);
                    poolingHttpClientConnectionManagerBuilder.setTlsSocketStrategy(new DefaultClientTlsStrategy(
                            sslContext,
                            HostnameVerificationPolicy.CLIENT,
                            isProdEnvironment() ? null : NoopHostnameVerifier.INSTANCE));
                })
                .withDefaultRequestConfigCustomizer((builder) -> {
                    builder.setConnectionKeepAlive(TimeValue.ofSeconds(10));
                    builder.setConnectionRequestTimeout(Timeout.ofSeconds(30));
                    builder.setResponseTimeout(Timeout.ofSeconds(60));
                });
    }

    private boolean isProdEnvironment() {
        return List.of(environment.getActiveProfiles()).contains("prod");
    }

    @Bean
    RestClientCustomizer restClientCustomizer(ObservationRegistry observationRegistry
            /** , LogbookClientHttpRequestInterceptor interceptor */
            ) {
        return restClientBuilder -> restClientBuilder
                .defaultHeaders(httpHeaders -> {
                    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                    httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
                })
                .baseUrl(applicationProperties.getPostServiceUrl())
                .observationRegistry(observationRegistry);
        // .requestInterceptor(interceptor);
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
