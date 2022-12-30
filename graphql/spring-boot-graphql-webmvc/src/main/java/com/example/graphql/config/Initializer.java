package com.example.graphql.config;

import com.example.graphql.entities.Author;
import com.example.graphql.entities.Post;
import com.example.graphql.entities.PostComment;
import com.example.graphql.entities.PostDetails;
import com.example.graphql.repositories.AuthorRepository;
import java.time.LocalDateTime;
import java.util.stream.LongStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class Initializer implements CommandLineRunner {

    private final AuthorRepository authorRepository;

    @Override
    public void run(String... args) {
        log.info("Running Initializer.....");
        this.authorRepository.deleteAll();

        LongStream.range(1, 5)
                .forEach(
                        i -> {
                            LocalDateTime localDateTime1 = LocalDateTime.now();
                            PostComment post1Comment =
                                    PostComment.builder()
                                            .title("Sample Review" + i)
                                            .published(true)
                                            .build();
                            PostComment post1Comment2 =
                                    PostComment.builder()
                                            .title("Complicated Review" + i)
                                            .published(false)
                                            .build();
                            PostDetails post1Details =
                                    PostDetails.builder()
                                            .createdBy("user" + i)
                                            .createdAt(localDateTime1)
                                            .key("key" + i)
                                            .build();
                            Post post =
                                    Post.builder()
                                            .title("Title" + i)
                                            .content("content" + 1)
                                            .createdAt(localDateTime1)
                                            .published(true)
                                            .build();
                            post.setDetails(post1Details);
                            post.addComment(post1Comment);
                            post.addComment(post1Comment2);

                            LocalDateTime localDateTime2 = LocalDateTime.now();
                            Post post1 =
                                    Post.builder()
                                            .title("Second Title" + i)
                                            .content("Second Content" + 1)
                                            .createdAt(localDateTime2)
                                            .published(false)
                                            .build();
                            PostComment post2Comment =
                                    PostComment.builder()
                                            .title("Complicated Title" + i)
                                            .published(true)
                                            .publishedAt(localDateTime2)
                                            .build();
                            PostDetails post2Details =
                                    PostDetails.builder()
                                            .createdBy("user" + i)
                                            .createdAt(localDateTime2)
                                            .key("keys" + i)
                                            .build();
                            post1.setDetails(post2Details);
                            post1.addComment(post2Comment);
                            Author author =
                                    Author.builder()
                                            .email("user" + i + "@example.com")
                                            .firstName("first name" + i)
                                            .lastName("last name" + i)
                                            .build();
                            author.addPost(post);
                            author.addPost(post1);
                            this.authorRepository.save(author);
                        });
    }
}
