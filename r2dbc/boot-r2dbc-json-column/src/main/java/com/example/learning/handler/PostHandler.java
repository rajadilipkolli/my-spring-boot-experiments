package com.example.learning.handler;

import static org.springframework.web.reactive.function.server.ServerResponse.*;

import com.example.learning.entity.Post;
import com.example.learning.repository.CommentRepository;
import com.example.learning.repository.PostRepository;
import java.net.URI;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
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
        return ok().body(
                        this.postRepository.findAll().flatMap(post -> commentRepository
                                .findByPostId(post.getId())
                                .collectList()
                                .map(comments -> {
                                    post.setComments(comments);
                                    return post;
                                })),
                        Post.class);
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
        var existed = this.postRepository.findById(UUID.fromString(req.pathVariable("id")));
        return Mono.zip(
                        (data) -> {
                            Post p = (Post) data[0];
                            Post p2 = (Post) data[1];
                            p.setTitle(p2.getTitle());
                            p.setContent(p2.getContent());
                            p.setMetadata(p2.getMetadata());
                            p.setStatus(p2.getStatus());
                            return p;
                        },
                        existed,
                        req.bodyToMono(Post.class))
                .cast(Post.class)
                .flatMap(this.postRepository::save)
                .flatMap(post -> noContent().build());
    }

    public Mono<ServerResponse> delete(ServerRequest req) {
        return this.postRepository
                .deleteById(UUID.fromString(req.pathVariable("id")))
                .then(Mono.defer(() -> noContent().build()));
    }
}
