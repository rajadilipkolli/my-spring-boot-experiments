package com.example.demo.config;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.noContent;

import com.example.demo.notifier.Notifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Configuration
class WebRouterConfig {

    @Bean
    RouterFunction<ServerResponse> routes(Notifier notifier) {
        return route().GET("/hello", request -> notifier.send()
                        .then(Mono.defer(() -> noContent().build())))
                .POST("/api/notify", contentType(APPLICATION_JSON).and(accept(APPLICATION_JSON)), notifier::notifyData)
                .build();
    }
}
