package com.example.highrps.post.api;

import com.example.highrps.post.command.CreatePostCommand;
import com.example.highrps.post.command.PostCommandResult;
import com.example.highrps.post.command.PostCommandService;
import com.example.highrps.post.command.UpdatePostCommand;
import com.example.highrps.post.domain.requests.NewPostRequest;
import com.example.highrps.post.query.PostQuery;
import com.example.highrps.post.query.PostQueryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Validated
@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostCommandService postCommandService;
    private final PostQueryService postQueryService;

    /**
     * Creates a post controller.
     *
     * @param postCommandService post write service
     * @param postQueryService post read service
     */
    public PostController(PostCommandService postCommandService, PostQueryService postQueryService) {
        this.postCommandService = postCommandService;
        this.postQueryService = postQueryService;
    }

    /**
     * Retrieves a post by its public identifier.
     *
     * @param postId the post identifier
     * @return the serialized post
     */
    @GetMapping(value = "/{postId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getPostByPostId(@PathVariable @Positive Long postId) {
        String postJson = postQueryService.getPost(new PostQuery(postId));
        return ResponseEntity.ok(postJson);
    }

    /**
     * Creates a post.
     *
     * @param newPostRequest the post details
     * @return a future containing the created post response
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<PostCommandResult>> createPost(
            @RequestBody @Valid NewPostRequest newPostRequest) {
        CreatePostCommand cmd = CreatePostCommand.fromNewPostRequest(newPostRequest);
        var uriBuilder = ServletUriComponentsBuilder.fromCurrentRequest();
        return postCommandService.createPost(cmd).thenApply(postCommandResult -> {
            URI location = uriBuilder
                    .path("/{postId}")
                    .buildAndExpand(postCommandResult.postId())
                    .toUri();
            return ResponseEntity.created(location).body(postCommandResult);
        });
    }

    /**
     * Updates a post.
     *
     * @param postId the post identifier
     * @param updatePostRequest the replacement post details
     * @return a future containing the updated post response
     */
    @PutMapping(
            value = "/{postId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<PostCommandResult>> updatePost(
            @PathVariable @Positive Long postId, @RequestBody @Valid NewPostRequest newPostRequest) {
        UpdatePostCommand cmd = UpdatePostCommand.fromNewPostRequest(newPostRequest, postId);
        return postCommandService.updatePost(cmd).thenApply(ResponseEntity::ok);
    }

    /**
     * Deletes a post.
     *
     * @param postId the post identifier
     * @return a future containing an empty response
     */
    @DeleteMapping(value = "/{postId}")
    public CompletableFuture<ResponseEntity<Void>> deletePost(@PathVariable @Positive Long postId) {
        return postCommandService
                .deletePost(postId)
                .thenApply(v -> ResponseEntity.noContent().build());
    }
}
