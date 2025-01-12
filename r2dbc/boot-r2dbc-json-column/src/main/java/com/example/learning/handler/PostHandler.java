package com.example.learning.handler;

import static org.springframework.web.reactive.function.server.ServerResponse.*;

import com.example.learning.entity.Comment;
import com.example.learning.entity.Post;
import com.example.learning.model.response.PagedResult;
import com.example.learning.repository.CommentRepository;
import com.example.learning.repository.PostRepository;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Component
public class PostHandler {

    private static final Logger log = LoggerFactory.getLogger(PostHandler.class);

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public PostHandler(PostRepository postRepository, CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    public Mono<ServerResponse> all(ServerRequest req) {
        int page = Integer.parseInt(req.queryParam("page").orElse("0"));
        int size = Integer.parseInt(req.queryParam("size").orElse("10"));
        return this.postRepository
                .findAllWithPagination(page * size, size)
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
                .zipWith(this.postRepository.count())
                .map(tuple -> {
                    List<Post> posts = tuple.getT1();
                    Long totalElements = tuple.getT2();
                    PageImpl<Post> postsPage = new PageImpl<>(posts, Pageable.ofSize(size), totalElements);
                    return new PagedResult<>(postsPage);
                })
                .flatMap(response -> ok().bodyValue(response));
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
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, String.format("Post not found with id: %s", req.pathVariable("id")))))
                .flatMap(existingPost -> req.bodyToMono(Post.class).map(updatedPost -> {
                    existingPost.setTitle(updatedPost.getTitle());
                    existingPost.setContent(updatedPost.getContent());
                    existingPost.setMetadata(updatedPost.getMetadata());
                    existingPost.setStatus(updatedPost.getStatus());
                    return existingPost;
                }))
                .flatMap(this.postRepository::save)
                .flatMap(post -> noContent().build())
                .onErrorResume(IllegalArgumentException.class, e -> {
                    log.error("Error updating post: {}", e.getMessage());
                    return ServerResponse.badRequest().bodyValue(e.getMessage());
                });
    }

    public Mono<ServerResponse> delete(ServerRequest req) {
        return this.postRepository
                .deleteById(UUID.fromString(req.pathVariable("id")))
                .then(Mono.defer(() -> noContent().build()));
    }
}
