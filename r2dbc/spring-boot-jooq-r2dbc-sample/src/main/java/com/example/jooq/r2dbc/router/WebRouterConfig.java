package com.example.jooq.r2dbc.router;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import com.example.jooq.r2dbc.handler.PostHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration(proxyBeanMethods = false)
public class WebRouterConfig {

    @Bean
    RouterFunction<ServerResponse> routerFunction(PostHandler handler) {
        return route().GET("/posts", handler::getAll).POST("/posts", handler::create).build();
    }
}
