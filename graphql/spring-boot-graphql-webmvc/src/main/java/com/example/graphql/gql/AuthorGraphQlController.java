package com.example.graphql.gql;

import com.example.graphql.dtos.PostInfo;
import com.example.graphql.entities.Author;
import com.example.graphql.entities.PostComment;
import com.example.graphql.services.AuthorService;
import com.example.graphql.services.PostCommentService;
import com.example.graphql.services.PostService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

@Controller
@Validated
@Slf4j
@RequiredArgsConstructor
public class AuthorGraphQlController {

    private final AuthorService authorService;

    private final PostService postService;

    private final PostCommentService postCommentService;

    @BatchMapping(typeName = "Author")
    public Map<Author, List<PostInfo>> posts(List<Author> authors) {
        List<Long> authorIds = authors.stream().map(Author::getId).toList();

        var authorPostsMap = this.postService.getPostByAuthorIdIn(authorIds);

        return authors.stream()
                .collect(
                        Collectors.toMap(
                                author -> author,
                                author ->
                                        authorPostsMap.getOrDefault(
                                                author.getId(), new ArrayList<>())));
    }

    @BatchMapping(typeName = "Post")
    public Map<PostInfo, List<PostComment>> comments(List<PostInfo> posts) {
        List<Long> postIds = posts.stream().map(PostInfo::getId).toList();

        var postCommentsMap = this.postCommentService.getCommentsByPostIdIn(postIds);

        return posts.stream()
                .collect(
                        Collectors.toMap(
                                post -> post,
                                post ->
                                        postCommentsMap.getOrDefault(
                                                post.getId(), new ArrayList<>())));
    }

    @QueryMapping
    public List<Author> allAuthors() {
        return this.authorService.findAllAuthors();
    }
}
