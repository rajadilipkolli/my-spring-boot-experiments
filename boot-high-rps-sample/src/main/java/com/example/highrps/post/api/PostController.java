package com.example.highrps.post.api;

import com.example.highrps.post.command.CreatePostCommand;
import com.example.highrps.post.command.PostCommandResult;
import com.example.highrps.post.command.PostCommandService;
import com.example.highrps.post.command.UpdatePostCommand;
import com.example.highrps.post.domain.requests.NewPostRequest;
import com.example.highrps.post.query.PostProjection;
import com.example.highrps.post.query.PostQuery;
import com.example.highrps.post.query.PostQueryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
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

    @GetMapping("/posts/{postId}")
    public ResponseEntity<PostProjection> getPostByPostId(@PathVariable @Positive Long postId) {
        PostProjection postProjection = postQueryService.getPost(new PostQuery(postId));
        return ResponseEntity.ok(postProjection);
    }

    @PostMapping(value = "/posts")
    public ResponseEntity<PostCommandResult> createPost(@RequestBody @Valid NewPostRequest newPostRequest) {
        CreatePostCommand cmd = new CreatePostCommand(
                newPostRequest.postId(),
                newPostRequest.title(),
                newPostRequest.content(),
                newPostRequest.email(),
                newPostRequest.published(),
                newPostRequest.details(),
                newPostRequest.tags());
        PostCommandResult postCommandResult = postCommandService.createPost(cmd);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{postId}")
                .buildAndExpand(postCommandResult.postId())
                .toUri();
        return ResponseEntity.created(location).body(postCommandResult);
    }

    @PutMapping(value = "/posts/{postId}")
    public ResponseEntity<PostCommandResult> updatePost(
            @PathVariable @Positive Long postId, @RequestBody @Valid NewPostRequest newPostRequest) {
        UpdatePostCommand cmd = new UpdatePostCommand(
                postId,
                newPostRequest.title(),
                newPostRequest.content(),
                newPostRequest.published(),
                newPostRequest.details(),
                newPostRequest.tags());
        PostCommandResult postCommandResult = postCommandService.updatePost(cmd);
        return ResponseEntity.ok(postCommandResult);
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable @Positive Long postId) {
        postCommandService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }
}
