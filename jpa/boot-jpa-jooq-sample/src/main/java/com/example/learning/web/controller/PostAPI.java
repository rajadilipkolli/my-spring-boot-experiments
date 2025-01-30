package com.example.learning.web.controller;

import com.example.learning.model.request.PostRequest;
import com.example.learning.model.response.PostResponse;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
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
                        content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ProblemDetail.class))
                        }),
            })
    @Tag(name = "Post API")
    ResponseEntity<Object> createPostByUserName(
            @Valid @RequestBody PostRequest postRequest,
            @NotBlank @Parameter(description = "Username of the post creator") String userName);

    @Operation(summary = "Retrieves a post by username and title")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Post retrieved successfully",
                        content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = PostResponse.class))
                        }),
                @ApiResponse(
                        responseCode = "404",
                        description = "Post not found",
                        content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ProblemDetail.class))
                        }),
            })
    @Tag(name = "Post API")
    ResponseEntity<PostResponse> getPostByUserNameAndTitle(
            @NotBlank @Parameter(description = "Username of the post creator") String userName,
            @NotBlank @Parameter(description = "Title of the post to retrieve") String title);

    @Operation(summary = "Updates a post by username and title")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Post updated successfully",
                        content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = PostResponse.class))
                        }),
                @ApiResponse(
                        responseCode = "400",
                        description = "Invalid input",
                        content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ProblemDetail.class))
                        }),
                @ApiResponse(
                        responseCode = "404",
                        description = "Post not found",
                        content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ProblemDetail.class))
                        }),
            })
    ResponseEntity<PostResponse> updatePostByUserNameAndTitle(
            @RequestBody @Valid PostRequest postRequest,
            @NotBlank @Parameter(name = "user_name", description = "Username of the post creator", in = ParameterIn.PATH)
                    String userName,
            @NotBlank @Parameter(description = "Title of the post to update", in = ParameterIn.PATH) String title);

    @Operation(summary = "Deletes a post by username and title")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "Post deleted successfully", content = @Content),
                @ApiResponse(
                        responseCode = "404",
                        description = "Post not found",
                        content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ProblemDetail.class))
                        }),
            })
    ResponseEntity<Void> deletePostByUserNameAndTitle(
            @NotBlank @Parameter(name = "user_name", description = "Username of the post creator", in = ParameterIn.PATH)
                    String userName,
            @NotBlank @Parameter(description = "Title of the post to delete", in = ParameterIn.PATH) String title);
}
