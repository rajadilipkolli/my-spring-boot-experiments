package com.example.learning.config;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import com.example.learning.handler.CommentHandler;
import com.example.learning.handler.PostHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration(proxyBeanMethods = false)
public class WebRouterConfig {

    @Bean
    RouterFunction<ServerResponse> routes(PostHandler postController, CommentHandler commentHandler) {
        return route().path("/posts", () -> route().nest(
                                path(""), () -> route().GET("", accept(APPLICATION_JSON), postController::all)
                                        .POST(
                                                "",
                                                contentType(APPLICATION_JSON).and(accept(APPLICATION_JSON)),
                                                postController::create)
                                        .build())
                        .nest(path("{id}"), () -> route().GET("", accept(APPLICATION_JSON), postController::get)
                                .PUT(
                                        "",
                                        contentType(APPLICATION_JSON).and(accept(APPLICATION_JSON)),
                                        postController::update)
                                .DELETE("", postController::delete)
                                .nest(path("comments"), () -> route().GET(
                                                "", accept(APPLICATION_JSON), commentHandler::getByPostId)
                                        .POST(
                                                "",
                                                contentType(APPLICATION_JSON).and(accept(APPLICATION_JSON)),
                                                commentHandler::create)
                                        .build())
                                .build())
                        .build())
                .build();
    }
}
