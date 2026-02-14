package com.example.highrps.postcomment.rest;

import com.example.highrps.postcomment.domain.*;
import com.example.highrps.postcomment.domain.vo.PostCommentId;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Validated
@RestController
@RequestMapping("/posts/{postId}/comments")
public class PostCommentController {
    private final PostCommentCommandService commandService;
    private final PostCommentQueryService queryService;

    public PostCommentController(PostCommentCommandService commandService, PostCommentQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @GetMapping
    public ResponseEntity<List<PostCommentResponse>> getAllComments(@PathVariable Long postId) {
        List<PostCommentResult> results = queryService.getCommentsByPostId(postId);
        return ResponseEntity.ok(results.stream().map(PostCommentResponse::from).toList());
    }

    @GetMapping("/{postCommentId}")
    public ResponseEntity<PostCommentResponse> getComment(@PathVariable Long postId, @PathVariable Long postCommentId) {
        PostCommentResult result =
                queryService.getCommentById(new GetPostCommentQuery(postId, PostCommentId.of(postCommentId)));
        return ResponseEntity.ok(PostCommentResponse.from(result));
    }

    @PostMapping
    public ResponseEntity<PostCommentResponse> createComment(
            @PathVariable Long postId, @RequestBody @Valid CreatePostCommentRequest request) {
        PostCommentId id = commandService.createComment(
                new CreatePostCommentCmd(request.title(), request.content(), postId, request.published()));

        PostCommentResult result = queryService.getCommentById(new GetPostCommentQuery(postId, id));

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{postCommentId}")
                .buildAndExpand(id.id())
                .toUri();

        return ResponseEntity.created(location).body(PostCommentResponse.from(result));
    }

    @PutMapping("/{postCommentId}")
    public ResponseEntity<PostCommentResponse> updateComment(
            @PathVariable Long postId,
            @PathVariable Long postCommentId,
            @RequestBody @Valid UpdatePostCommentRequest request) {
        commandService.updateComment(new UpdatePostCommentCmd(
                PostCommentId.of(postCommentId), postId, request.title(), request.content(), request.published()));

        PostCommentResult result =
                queryService.getCommentById(new GetPostCommentQuery(postId, PostCommentId.of(postCommentId)));
        return ResponseEntity.ok(PostCommentResponse.from(result));
    }

    @DeleteMapping("/{postCommentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long postId, @PathVariable Long postCommentId) {
        commandService.deleteComment(PostCommentId.of(postCommentId), postId);
        return ResponseEntity.noContent().build();
    }
}
