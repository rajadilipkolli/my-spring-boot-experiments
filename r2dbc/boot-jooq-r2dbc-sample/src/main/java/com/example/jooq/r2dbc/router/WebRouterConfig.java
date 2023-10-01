package com.example.jooq.r2dbc.router;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import com.example.jooq.r2dbc.entities.Post;
import com.example.jooq.r2dbc.entities.Tags;
import com.example.jooq.r2dbc.handler.PostHandler;
import com.example.jooq.r2dbc.handler.TagHandler;
import com.example.jooq.r2dbc.model.request.CreatePostCommand;
import com.example.jooq.r2dbc.model.request.CreatePostComment;
import com.example.jooq.r2dbc.model.request.TagDto;
import com.example.jooq.r2dbc.model.response.PaginatedResult;
import com.example.jooq.r2dbc.model.response.PostSummary;
import com.example.jooq.r2dbc.utils.AppConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration(proxyBeanMethods = false)
public class WebRouterConfig {

    @RouterOperations({
        @RouterOperation(
                path = "/posts",
                method = RequestMethod.GET,
                headers = {"x-header1=test1", "x-header2=test2"},
                operation =
                        @Operation(
                                operationId = "all",
                                parameters = {
                                    @Parameter(name = "key", description = "sample description"),
                                    @Parameter(name = "test", description = "sample desc")
                                },
                                responses =
                                        @ApiResponse(
                                                responseCode = "200",
                                                content =
                                                        @Content(
                                                                array =
                                                                        @ArraySchema(
                                                                                schema =
                                                                                        @Schema(
                                                                                                implementation =
                                                                                                        PostSummary
                                                                                                                .class)))))),
        @RouterOperation(
                path = "/posts/search",
                method = RequestMethod.GET,
                operation =
                        @Operation(
                                operationId = "search",
                                description = "search posts based on title",
                                summary =
                                        "searches based on title and fetches associated comments and tags using pagination",
                                parameters = {
                                    @Parameter(
                                            name = "keyword",
                                            description = "keyword for searching",
                                            in = ParameterIn.QUERY,
                                            required = true),
                                    @Parameter(
                                            name = "pageNo",
                                            description = "page Number of page",
                                            in = ParameterIn.QUERY,
                                            example = "0"),
                                    @Parameter(
                                            name = "pageSize",
                                            description = "max Number of records per page",
                                            in = ParameterIn.QUERY,
                                            example = "10"),
                                    @Parameter(
                                            name = "sortBy",
                                            description = "sort By Fields",
                                            in = ParameterIn.QUERY,
                                            example = AppConstants.DEFAULT_SORT_BY),
                                    @Parameter(
                                            name = "sortDir",
                                            description = "sortBy Direction asc/desc",
                                            in = ParameterIn.QUERY,
                                            example = AppConstants.DEFAULT_SORT_DIRECTION)
                                },
                                responses =
                                        @ApiResponse(
                                                responseCode = "200",
                                                content =
                                                        @Content(
                                                                schema =
                                                                        @Schema(
                                                                                implementation =
                                                                                        PaginatedResult
                                                                                                .class))))),
        @RouterOperation(
                path = "/posts",
                method = RequestMethod.POST,
                operation =
                        @Operation(
                                operationId = "create",
                                requestBody =
                                        @RequestBody(
                                                content =
                                                        @Content(
                                                                schema =
                                                                        @Schema(
                                                                                implementation =
                                                                                        CreatePostCommand
                                                                                                .class))),
                                responses = @ApiResponse(responseCode = "201"))),
        @RouterOperation(
                path = "/posts/comments/{id}",
                method = RequestMethod.POST,
                operation =
                        @Operation(
                                operationId = "createComment",
                                parameters = @Parameter(name = "id", in = ParameterIn.PATH),
                                requestBody =
                                        @RequestBody(
                                                content =
                                                        @Content(
                                                                schema =
                                                                        @Schema(
                                                                                implementation =
                                                                                        CreatePostComment
                                                                                                .class))),
                                responses = @ApiResponse(responseCode = "201"))),
        @RouterOperation(
                path = "/posts/{id}",
                method = RequestMethod.GET,
                operation =
                        @Operation(
                                operationId = "get",
                                parameters = @Parameter(name = "id", in = ParameterIn.PATH),
                                responses =
                                        @ApiResponse(
                                                responseCode = "200",
                                                content =
                                                        @Content(
                                                                schema =
                                                                        @Schema(
                                                                                implementation =
                                                                                        Post
                                                                                                .class))))),
        @RouterOperation(
                path = "/posts/{id}",
                method = RequestMethod.PUT,
                operation =
                        @Operation(
                                operationId = "update",
                                parameters = @Parameter(name = "id", in = ParameterIn.PATH),
                                responses =
                                        @ApiResponse(
                                                responseCode = "202",
                                                content =
                                                        @Content(
                                                                schema =
                                                                        @Schema(
                                                                                implementation =
                                                                                        Post
                                                                                                .class)))))
    })
    @Bean
    RouterFunction<ServerResponse> postsRouterFunction(PostHandler handler) {
        return route(GET("/posts"), handler::getAll)
                .andRoute(GET("/posts/search"), handler::search)
                .andRoute(POST("/posts"), handler::create)
                .andRoute(GET("/posts/{id}"), handler::get)
                .andRoute(PUT("/posts/{id}"), handler::update)
                .andRoute(POST("/posts/comments/{id}"), handler::createComments);
    }

    @RouterOperations({
        @RouterOperation(
                path = "/tags",
                method = RequestMethod.GET,
                operation =
                        @Operation(
                                operationId = "all",
                                summary = "fetches all tags from database using pagination",
                                parameters = {
                                    @Parameter(
                                            name = "pageNo",
                                            description = "page Number of page",
                                            in = ParameterIn.QUERY,
                                            example = "0"),
                                    @Parameter(
                                            name = "pageSize",
                                            description = "max Number of records per page",
                                            in = ParameterIn.QUERY,
                                            example = "10"),
                                    @Parameter(
                                            name = "sortBy",
                                            description = "sort By Fields",
                                            in = ParameterIn.QUERY,
                                            example = AppConstants.DEFAULT_SORT_BY),
                                    @Parameter(
                                            name = "sortDir",
                                            description = "sortBy Direction asc/desc",
                                            in = ParameterIn.QUERY,
                                            example = AppConstants.DEFAULT_SORT_DIRECTION)
                                },
                                responses =
                                        @ApiResponse(
                                                responseCode = "200",
                                                content =
                                                        @Content(
                                                                schema =
                                                                        @Schema(
                                                                                implementation =
                                                                                        PaginatedResult
                                                                                                .class))))),
        @RouterOperation(
                path = "/tags",
                method = RequestMethod.POST,
                operation =
                        @Operation(
                                operationId = "create",
                                requestBody =
                                        @RequestBody(
                                                content =
                                                        @Content(
                                                                schema =
                                                                        @Schema(
                                                                                implementation =
                                                                                        TagDto
                                                                                                .class))),
                                responses = @ApiResponse(responseCode = "201"))),
        @RouterOperation(
                path = "/tags/{id}",
                method = RequestMethod.GET,
                operation =
                        @Operation(
                                operationId = "get",
                                parameters = @Parameter(name = "id", in = ParameterIn.PATH),
                                responses =
                                        @ApiResponse(
                                                responseCode = "200",
                                                content =
                                                        @Content(
                                                                schema =
                                                                        @Schema(
                                                                                implementation =
                                                                                        Tags
                                                                                                .class)))))
    })
    @Bean
    RouterFunction<ServerResponse> tagsRouterFunction(TagHandler handler) {
        return route(GET("/tags"), handler::getAll)
                .andRoute(POST("/tags"), handler::create)
                .andRoute(GET("/tags/{id}"), handler::get);
    }
}
