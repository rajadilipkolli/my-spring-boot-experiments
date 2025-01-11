package com.example.learning.handler;

import static org.springframework.web.reactive.function.server.ServerResponse.*;

import com.example.learning.entity.Comment;
import com.example.learning.entity.Post;
import com.example.learning.repository.CommentRepository;
import com.example.learning.repository.PostRepository;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Component
public class PostHandler {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public PostHandler(PostRepository postRepository, CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    public Mono<ServerResponse> all(ServerRequest req) {
        return this.postRepository
                .findAll()
                .collectList()
                .flatMap(posts -> {
                    var postIds = posts.stream().map(Post::getId).collect(Collectors.toList());
                    return commentRepository
                            .findAllByPostIdIn(postIds)
                            .collectMultimap(Comment::getPostId)
                            .map(commentsByPost -> {
                                posts.forEach(post -> post.setComments(
                                        (List<Comment>) commentsByPost.getOrDefault(post.getId(), List.of())));
                                return posts;
                            });
                })
                .flatMap(posts -> ok().bodyValue(posts));
    }

    public Mono<ServerResponse> create(ServerRequest req) {
        return req.bodyToMono(Post.class)
                .flatMap(this.postRepository::save)
                .flatMap(post -> created(URI.create("/posts/" + post.getId())).build());
    }

    public Mono<ServerResponse> get(ServerRequest req) {
        return this.postRepository
                .findById(UUID.fromString(req.pathVariable("id")))
                .flatMap(post -> ok().body(Mono.just(post), Post.class))
                .switchIfEmpty(notFound().build());
    }

    public Mono<ServerResponse> update(ServerRequest req) {
        return this.postRepository
                .findById(UUID.fromString(req.pathVariable("id")))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found")))
                .flatMap(existingPost -> req.bodyToMono(Post.class).map(updatedPost -> {
                    existingPost.setTitle(updatedPost.getTitle());
                    existingPost.setContent(updatedPost.getContent());
                    existingPost.setMetadata(updatedPost.getMetadata());
                    existingPost.setStatus(updatedPost.getStatus());
                    return existingPost;
                }))
                .flatMap(this.postRepository::save)
                .flatMap(post -> noContent().build())
                .onErrorResume(IllegalArgumentException.class, e -> ServerResponse.badRequest()
                        .bodyValue(e.getMessage()));
    }

    public Mono<ServerResponse> delete(ServerRequest req) {
        return this.postRepository
                .deleteById(UUID.fromString(req.pathVariable("id")))
                .then(Mono.defer(() -> noContent().build()));
    }
}
