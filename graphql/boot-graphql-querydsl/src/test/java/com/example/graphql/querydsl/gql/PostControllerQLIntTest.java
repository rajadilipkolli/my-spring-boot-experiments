package com.example.graphql.querydsl.gql;

import static com.example.graphql.querydsl.utils.TestData.getPost;

import com.example.graphql.querydsl.common.AbstractIntegrationTest;
import com.example.graphql.querydsl.entities.Post;
import com.example.graphql.querydsl.repositories.PostRepository;
import java.util.ArrayList;
import java.util.List;
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
                .isEqualTo(3);
    }
}
