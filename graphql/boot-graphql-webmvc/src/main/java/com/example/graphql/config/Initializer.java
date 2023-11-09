package com.example.graphql.config;

import com.example.graphql.entities.AuthorEntity;
import com.example.graphql.entities.PostCommentEntity;
import com.example.graphql.entities.PostDetailsEntity;
import com.example.graphql.entities.PostEntity;
import com.example.graphql.repositories.AuthorRepository;
import java.time.OffsetDateTime;
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
                            PostCommentEntity post1Comment =
                                    new PostCommentEntity()
                                            .setTitle("Sample Review" + i)
                                            .setContent("Sample Content" + i)
                                            .setPublished(true);
                            PostCommentEntity post1Comment2 =
                                    new PostCommentEntity()
                                            .setTitle("Complicated Review" + i)
                                            .setContent("Complicated Content" + i)
                                            .setPublished(false);
                            PostDetailsEntity post1Details =
                                    new PostDetailsEntity()
                                            .setCreatedBy("user" + i)
                                            .setDetailsKey("key" + i);
                            PostEntity postEntity =
                                    new PostEntity()
                                            .setTitle("Title" + i)
                                            .setContent("content" + 1)
                                            .setPublished(true);
                            postEntity.setDetails(post1Details);
                            postEntity.addComment(post1Comment);
                            postEntity.addComment(post1Comment2);

                            PostEntity postEntity1 =
                                    new PostEntity()
                                            .setTitle("Second Title" + i)
                                            .setContent("Second Content" + 1)
                                            .setPublished(false);
                            PostCommentEntity post2Comment =
                                    new PostCommentEntity()
                                            .setTitle("Complicated Title" + i)
                                            .setContent("Complicated Content" + i)
                                            .setPublished(true)
                                            .setPublishedAt(OffsetDateTime.now());
                            PostDetailsEntity post2Details =
                                    new PostDetailsEntity()
                                            .setCreatedBy("user" + i)
                                            .setDetailsKey("keys" + i);
                            postEntity1.setDetails(post2Details);
                            postEntity1.addComment(post2Comment);
                            AuthorEntity authorEntity =
                                    new AuthorEntity()
                                            .setEmail("user" + i + "@example.com")
                                            .setFirstName("first name" + i)
                                            .setLastName("last name" + i)
                                            .setMobile(9848922338L);
                            authorEntity.addPost(postEntity);
                            authorEntity.addPost(postEntity1);
                            this.authorRepository.save(authorEntity);
                        });
    }
}
