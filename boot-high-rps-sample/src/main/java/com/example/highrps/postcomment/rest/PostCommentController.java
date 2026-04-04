package com.example.highrps.postcomment.rest;

import com.example.highrps.postcomment.command.CreatePostCommentCommand;
import com.example.highrps.postcomment.command.PostCommentCommandResult;
import com.example.highrps.postcomment.command.PostCommentCommandService;
import com.example.highrps.postcomment.command.UpdatePostCommentCommand;
import com.example.highrps.postcomment.domain.vo.PostCommentId;
import com.example.highrps.postcomment.query.GetPostCommentQuery;
import com.example.highrps.postcomment.query.PostCommentQueryService;
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
    public ResponseEntity<List<PostCommentCommandResult>> getAllComments(@PathVariable @Positive Long postId) {
        return ResponseEntity.ok(queryService.getCommentsByPostId(postId));
    }

    @GetMapping("/{postCommentId}")
    public ResponseEntity<PostCommentCommandResult> getComment(
            @PathVariable @Positive Long postId, @PathVariable @Positive Long postCommentId) {
        PostCommentCommandResult result =
                queryService.getCommentById(new GetPostCommentQuery(postId, PostCommentId.of(postCommentId)));
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<PostCommentCommandResult> createComment(
            @PathVariable @Positive Long postId, @RequestBody @Valid CreatePostCommentRequest request) {
        PostCommentCommandResult result = commandService.createComment(
                new CreatePostCommentCommand(request.title(), request.content(), postId, request.published()));

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{postCommentId}")
                .buildAndExpand(result.id())
                .toUri();

        return ResponseEntity.created(location).body(result);
    }

    @PutMapping("/{postCommentId}")
    public ResponseEntity<PostCommentCommandResult> updateComment(
            @PathVariable @Positive Long postId,
            @PathVariable @Positive Long postCommentId,
            @RequestBody @Valid UpdatePostCommentRequest request) {
        commandService.updateComment(new UpdatePostCommentCommand(
                PostCommentId.of(postCommentId), postId, request.title(), request.content(), request.published()));

        PostCommentCommandResult result =
                queryService.getCommentById(new GetPostCommentQuery(postId, PostCommentId.of(postCommentId)));
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{postCommentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable @Positive Long postId, @PathVariable @Positive Long postCommentId) {
        commandService.deleteComment(PostCommentId.of(postCommentId), postId);
        return ResponseEntity.noContent().build();
    }
}
