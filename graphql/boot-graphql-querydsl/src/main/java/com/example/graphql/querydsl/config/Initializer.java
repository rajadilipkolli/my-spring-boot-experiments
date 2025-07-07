package com.example.graphql.querydsl.config;

import com.example.graphql.querydsl.entities.Post;
import com.example.graphql.querydsl.entities.PostComment;
import com.example.graphql.querydsl.entities.PostDetails;
import com.example.graphql.querydsl.repositories.PostRepository;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
class Initializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(Initializer.class);

    private final PostRepository postRepository;

    public Initializer(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    public void run(String... args) {
        log.info("Running Initializer.....");
        postRepository.deleteAll();
        Post post = new Post().setTitle("title").setContent("content");
        post.addDetails(new PostDetails()
                .setCreatedOn(LocalDateTime.of(2023, 12, 31, 10, 35, 45, 99))
                .setCreatedBy("appUser"));
        post.addComment(
                new PostComment().setReview("review").setCreatedOn(LocalDateTime.of(2023, 12, 31, 10, 35, 45, 99)));
        postRepository.save(post);
    }
}
