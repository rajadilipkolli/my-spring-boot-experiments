package com.example.learning.web.controller;

import com.example.learning.model.request.PostRequest;
import com.example.learning.model.response.PostResponse;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@OpenAPIDefinition(
        info =
                @Info(
                        title = "Post API",
                        version = "1.0",
                        description = "REST API for managing blog posts and associated content"),
        tags = {@Tag(name = "Post API", description = "performs all CRUD operations for posts")})
public interface PostAPI {

    @Operation(summary = "Creates post")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "201",
                        description = "Post Created",
                        content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE)}),
                @ApiResponse(
                        responseCode = "409",
                        description = "Post with same title exists",
                        content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE)})
            })
    @Tag(name = "Post API")
    ResponseEntity<Object> createPostByUserName(
            @Valid @RequestBody PostRequest postRequest,
            @NotBlank @Parameter(description = "Username of the post creator") String userName);

    @Tag(name = "Post API")
    ResponseEntity<PostResponse> getPostByUserNameAndTitle(
            @Parameter(description = "Username of the post creator") String userName,
            @Parameter(description = "Title of the post to retrieve") String title);
}
