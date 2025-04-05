package com.example.learning.handler;

import static org.springframework.web.reactive.function.server.ServerResponse.created;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import com.example.learning.entity.Comment;
import com.example.learning.repository.CommentRepository;
import io.micrometer.observation.annotation.Observed;
import java.net.URI;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@Observed(name = "commentService")
public class CommentHandler {

    private final CommentRepository comments;

    public CommentHandler(CommentRepository comments) {
        this.comments = comments;
    }

    public Mono<ServerResponse> create(ServerRequest req) {
        var postId = UUID.fromString(req.pathVariable("id"));
        return req.bodyToMono(Comment.class)
                .map(comment -> {
                    comment.setPostId(postId);
                    return comment;
                })
                .flatMap(this.comments::save)
                .flatMap(c -> created(URI.create("/posts/" + postId + "/comments/" + c.getId()))
                        .build());
    }

    public Mono<ServerResponse> getByPostId(ServerRequest req) {
        var result = this.comments.findByPostId(UUID.fromString(req.pathVariable("id")));
        return ok().body(result, Comment.class);
    }
}
