package com.example.highrps.postcomment.rest;

import com.example.highrps.postcomment.domain.CreatePostCommentCmd;
import com.example.highrps.postcomment.domain.GetPostCommentQuery;
import com.example.highrps.postcomment.domain.PostCommentCommandService;
import com.example.highrps.postcomment.domain.PostCommentQueryService;
import com.example.highrps.postcomment.domain.PostCommentResult;
import com.example.highrps.postcomment.domain.UpdatePostCommentCmd;
import com.example.highrps.postcomment.domain.vo.PostCommentId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
    public ResponseEntity<List<PostCommentResponse>> getAllComments(@PathVariable @Positive Long postId) {
        List<PostCommentResult> results = queryService.getCommentsByPostId(postId);
        return ResponseEntity.ok(results.stream().map(PostCommentResponse::from).toList());
    }

    @GetMapping("/{postCommentId}")
    public ResponseEntity<PostCommentResponse> getComment(
            @PathVariable @Positive Long postId, @PathVariable @Positive Long postCommentId) {
        PostCommentResult result =
                queryService.getCommentById(new GetPostCommentQuery(postId, PostCommentId.of(postCommentId)));
        return ResponseEntity.ok(PostCommentResponse.from(result));
    }

    @PostMapping
    public ResponseEntity<PostCommentResponse> createComment(
            @PathVariable @Positive Long postId, @RequestBody @Valid CreatePostCommentRequest request) {
        PostCommentResult result = commandService.createComment(
                new CreatePostCommentCmd(request.title(), request.content(), postId, request.published()));

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{postCommentId}")
                .buildAndExpand(result.commentId())
                .toUri();

        return ResponseEntity.created(location).body(PostCommentResponse.from(result));
    }

    @PutMapping("/{postCommentId}")
    public ResponseEntity<PostCommentResponse> updateComment(
            @PathVariable @Positive Long postId,
            @PathVariable @Positive Long postCommentId,
            @RequestBody @Valid UpdatePostCommentRequest request) {
        commandService.updateComment(new UpdatePostCommentCmd(
                PostCommentId.of(postCommentId), postId, request.title(), request.content(), request.published()));

        PostCommentResult result =
                queryService.getCommentById(new GetPostCommentQuery(postId, PostCommentId.of(postCommentId)));
        return ResponseEntity.ok(PostCommentResponse.from(result));
    }

    @DeleteMapping("/{postCommentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable @Positive Long postId, @PathVariable @Positive Long postCommentId) {
        commandService.deleteComment(PostCommentId.of(postCommentId), postId);
        return ResponseEntity.noContent().build();
    }
}
