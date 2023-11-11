package com.example.graphql.querydsl.gql;

import static com.example.graphql.querydsl.utils.TestData.getPost;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.graphql.querydsl.common.AbstractIntegrationTest;
import com.example.graphql.querydsl.entities.Post;
import com.example.graphql.querydsl.model.request.PostCommentRequest;
import com.example.graphql.querydsl.model.request.TagRequest;
import com.example.graphql.querydsl.model.response.PostCommentResponse;
import com.example.graphql.querydsl.model.response.TagResponse;
import com.example.graphql.querydsl.repositories.PostRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PostControllerQLIntTest extends AbstractIntegrationTest {

    @Autowired
    private PostRepository postRepository;

    private List<Post> postList = null;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();

        postList = new ArrayList<>();
        postList.add(getPost("First Post", "First Content"));
        postList.add(getPost("Second Post", "Second Content"));
        postList.add(getPost("Third Post", "Third Content"));
        postList = postRepository.saveAll(postList);
    }

    @Test
    void testCountPosts() {
        graphQlTester
                .document(
                        """
                        query {
                          countPosts
                        }
                        """)
                .execute()
                .path("countPosts")
                .entity(Integer.class)
                .isEqualTo(postList.size());
    }

    @Test
    void createPost() {
        Map<String, Object> inputValues = new HashMap<>();
        inputValues.put("title", "JunitTitle");
        inputValues.put("content", "JunitContent");
        inputValues.put("createdBy", "Junit");
        inputValues.put("comments", List.of(new PostCommentRequest("JunitReview")));
        inputValues.put("tags", List.of(new TagRequest("junit")));

        graphQlTester
                .documentName("createPost")
                .variable("createPostRequest", inputValues)
                .execute()
                .path("createPost.id")
                .entity(Long.class)
                .satisfies(id -> assertThat(id).isGreaterThan(0))
                .path("createPost.title")
                .entity(String.class)
                .isEqualTo("JunitTitle")
                .path("createPost.content")
                .entity(String.class)
                .isEqualTo("JunitContent")
                .path("createPost.createdOn")
                .entity(LocalDateTime.class)
                .path("createPost.comments")
                .entityList(PostCommentResponse.class)
                .hasSize(1)
                .satisfies(postCommentResponses -> {
                    assertThat(postCommentResponses.get(0).review()).isEqualTo("JunitReview");
                    assertThat(postCommentResponses.get(0).createdOn()).isInstanceOf(LocalDateTime.class);
                })
                .path("createPost.tags")
                .entityList(TagResponse.class)
                .hasSize(1)
                .satisfies(
                        tagResponses -> assertThat(tagResponses.get(0).name()).isEqualTo("junit"));
    }
}
