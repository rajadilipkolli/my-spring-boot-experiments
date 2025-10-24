package com.example.graphql.querydsl.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.example.graphql.querydsl.common.AbstractIntegrationTest;
import com.example.graphql.querydsl.entities.Post;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PostRepositoryIntTest extends AbstractIntegrationTest {

    @Autowired
    private PostRepository postRepository;

    @Test
    void findByDetailsCreatedByEqualsIgnoreCase_returnsPosts() {
        List<Post> posts = postRepository.findByDetailsCreatedByEqualsIgnoreCase("appUser");
        assertFalse(posts.isEmpty(), "expected at least one post for appUser");
        assertEquals("appUser", posts.getFirst().getDetails().getCreatedBy());
    }
}
