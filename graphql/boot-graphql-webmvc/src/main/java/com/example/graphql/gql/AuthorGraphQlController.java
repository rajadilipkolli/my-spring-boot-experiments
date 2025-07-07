package com.example.graphql.gql;

import com.example.graphql.entities.TagEntity;
import com.example.graphql.exception.AuthorNotFoundException;
import com.example.graphql.model.request.AuthorRequest;
import com.example.graphql.model.response.AuthorResponse;
import com.example.graphql.model.response.PostCommentResponse;
import com.example.graphql.projections.PostInfo;
import com.example.graphql.services.AuthorService;
import com.example.graphql.services.PostCommentService;
import com.example.graphql.services.PostService;
import com.example.graphql.services.TagService;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

@Controller
@Validated
public class AuthorGraphQlController {

    private final AuthorService authorService;

    private final PostService postService;

    private final PostCommentService postCommentService;

    private final TagService tagService;

    @BatchMapping(typeName = "Author")
    public Map<AuthorResponse, List<PostInfo>> posts(List<AuthorResponse> authorResponses) {
        log.info("Fetching PostInformation by AuthorIds");
        List<Long> authorIds = authorResponses.stream().map(AuthorResponse::id).toList();

        var authorPostsMap = this.postService.getPostByAuthorIdIn(authorIds);

        return authorResponses.stream()
                .collect(Collectors.toMap(
                        Function.identity(), author -> authorPostsMap.getOrDefault(author.id(), new ArrayList<>())));
    }

    @BatchMapping(typeName = "Post")
    public Map<PostInfo, List<PostCommentResponse>> comments(List<PostInfo> posts) {
        log.info("Fetching PostComments by PostIds");
        List<Long> postIds = posts.stream().map(PostInfo::getId).toList();

        var postCommentsMap = this.postCommentService.getCommentsByPostIdIn(postIds);

        return posts.stream()
                .collect(Collectors.toMap(
                        Function.identity(), post -> postCommentsMap.getOrDefault(post.getId(), new ArrayList<>())));
    }

    @BatchMapping(typeName = "Post")
    public Map<PostInfo, List<TagEntity>> tags(List<PostInfo> posts) {
        log.info("Fetching Tags by PostIds");
        List<Long> postIds = posts.stream().map(PostInfo::getId).toList();

        var postCommentsMap = this.tagService.getTagsByPostIdIn(postIds);

        return posts.stream()
                .collect(Collectors.toMap(
                        Function.identity(), post -> postCommentsMap.getOrDefault(post.getId(), new ArrayList<>())));
    }

    @QueryMapping
    public List<AuthorResponse> allAuthors() {
        return this.authorService.findAllAuthors();
    }

    @QueryMapping
    public AuthorResponse findAuthorByEmailId(@Argument("email") String email) {
        return this.authorService.findAuthorByEmailId(email).orElseThrow(() -> new AuthorNotFoundException(email));
    }

    @MutationMapping
    public AuthorResponse createAuthor(@Valid @Argument("authorInput") AuthorRequest authorRequest) {
        return this.authorService.saveAuthor(authorRequest);
    }
}
