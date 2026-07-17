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
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Validated
@RestController
public class PostController {

    private final PostCommandService postCommandService;
    private final PostQueryService postQueryService;

    public PostController(PostCommandService postCommandService, PostQueryService postQueryService) {
        this.postCommandService = postCommandService;
        this.postQueryService = postQueryService;
    }

    @GetMapping(value = "/posts/{postId}", produces = "application/json")
    public ResponseEntity<String> getPostByPostId(@PathVariable @Positive Long postId) {
        String postJson = postQueryService.getPost(new PostQuery(postId));
        return ResponseEntity.ok(postJson);
    }

    @PostMapping(value = "/posts")
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

    @PutMapping(value = "/posts/{postId}")
    public CompletableFuture<ResponseEntity<PostCommandResult>> updatePost(
            @PathVariable @Positive Long postId, @RequestBody @Valid NewPostRequest newPostRequest) {
        UpdatePostCommand cmd = UpdatePostCommand.fromNewPostRequest(newPostRequest, postId);
        return postCommandService.updatePost(cmd).thenApply(ResponseEntity::ok);
    }

    @DeleteMapping("/posts/{postId}")
    public CompletableFuture<ResponseEntity<Void>> deletePost(@PathVariable @Positive Long postId) {
        return postCommandService.deletePost(postId).thenApply(v -> ResponseEntity.noContent()
                .build());
    }
}
