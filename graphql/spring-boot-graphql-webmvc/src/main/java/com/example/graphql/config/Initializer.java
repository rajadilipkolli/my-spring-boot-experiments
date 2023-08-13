package com.example.graphql.config;

import com.example.graphql.entities.AuthorEntity;
import com.example.graphql.entities.PostCommentEntity;
import com.example.graphql.entities.PostDetailsEntity;
import com.example.graphql.entities.PostEntity;
import com.example.graphql.repositories.AuthorRepository;
import java.time.LocalDateTime;
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
                            LocalDateTime localDateTime1 = LocalDateTime.now();
                            PostCommentEntity post1Comment =
                                    PostCommentEntity.builder()
                                            .title("Sample Review" + i)
                                            .content("Sample Content" + i)
                                            .published(true)
                                            .build();
                            PostCommentEntity post1Comment2 =
                                    PostCommentEntity.builder()
                                            .title("Complicated Review" + i)
                                            .content("Complicated Content" + i)
                                            .published(false)
                                            .build();
                            PostDetailsEntity post1Details =
                                    PostDetailsEntity.builder()
                                            .createdBy("user" + i)
                                            .createdAt(localDateTime1)
                                            .detailsKey("key" + i)
                                            .build();
                            PostEntity postEntity =
                                    PostEntity.builder()
                                            .title("Title" + i)
                                            .content("content" + 1)
                                            .createdAt(OffsetDateTime.now())
                                            .published(true)
                                            .build();
                            postEntity.setDetails(post1Details);
                            postEntity.addComment(post1Comment);
                            postEntity.addComment(post1Comment2);

                            LocalDateTime localDateTime2 = LocalDateTime.now();
                            PostEntity postEntity1 =
                                    PostEntity.builder()
                                            .title("Second Title" + i)
                                            .content("Second Content" + 1)
                                            .createdAt(OffsetDateTime.now())
                                            .published(false)
                                            .build();
                            PostCommentEntity post2Comment =
                                    PostCommentEntity.builder()
                                            .title("Complicated Title" + i)
                                            .content("Complicated Content" + i)
                                            .published(true)
                                            .publishedAt(localDateTime2)
                                            .build();
                            PostDetailsEntity post2Details =
                                    PostDetailsEntity.builder()
                                            .createdBy("user" + i)
                                            .createdAt(localDateTime2)
                                            .detailsKey("keys" + i)
                                            .build();
                            postEntity1.setDetails(post2Details);
                            postEntity1.addComment(post2Comment);
                            AuthorEntity authorEntity =
                                    AuthorEntity.builder()
                                            .email("user" + i + "@example.com")
                                            .firstName("first name" + i)
                                            .lastName("last name" + i)
                                            .mobile(9848922338L)
                                            .build();
                            authorEntity.addPost(postEntity);
                            authorEntity.addPost(postEntity1);
                            this.authorRepository.save(authorEntity);
                        });
    }
}
