package com.example.learning.config;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import com.example.learning.handler.CommentHandler;
import com.example.learning.handler.PostHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class WebRouterConfig {

    @Bean
    public RouterFunction<ServerResponse> routes(PostHandler postController, CommentHandler commentHandler) {
        return route().path("/posts", () -> route().nest(path(""), () -> route().GET("", postController::all)
                                .POST("", postController::create)
                                .build())
                        .nest(path("{id}"), () -> route().GET("", postController::get)
                                .PUT("", postController::update)
                                .DELETE("", postController::delete)
                                .nest(path("comments"), () -> route().GET("", commentHandler::getByPostId)
                                        .POST("", commentHandler::create)
                                        .build())
                                .build())
                        .build())
                .build();
    }
}
