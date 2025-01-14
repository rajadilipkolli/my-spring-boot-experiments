package com.example.learning.web.controller;

import com.example.learning.model.request.PostRequest;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@OpenAPIDefinition(tags = {@Tag(name = "Post API", description = "performs all CRUD operations for posts")})
interface PostAPI {

    @Operation(summary = "Creates post")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "201",
                        description = "Post Created",
                        content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE)}),
                @ApiResponse(
                        responseCode = "419",
                        description = "Post with same title exists",
                        content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE)})
            })
    @Tag(name = "Post API")
    ResponseEntity<Object> createPostByUserName(
            PostRequest postRequest, @Parameter(description = "id of user who is creating") String userName);
}
