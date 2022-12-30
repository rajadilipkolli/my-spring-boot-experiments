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
        this.authorRepository.deleteAllInBatch();

        LongStream.range(1, 5)
                .forEach(
                        i -> {
                            PostComment postComment =
                                    PostComment.builder().review("Sample Review" + i).build();
                            PostComment postComment1 =
                                    PostComment.builder().review("Complicated Review" + i).build();
                            PostDetails postDetails =
                                    PostDetails.builder()
                                            .createdBy("user" + i)
                                            .createdOn(LocalDateTime.now())
                                            .build();
                            Post post =
                                    Post.builder()
                                            .title("Title" + i)
                                            .content("content" + 1)
                                            .createdOn(LocalDateTime.now())
                                            .build();
                            post.addDetails(postDetails);
                            post.addComment(postComment);
                            post.addComment(postComment1);

                            Post post1 =
                                    Post.builder()
                                            .title("Second Title" + i)
                                            .content("Second Content" + 1)
                                            .createdOn(LocalDateTime.now())
                                            .build();
                            post1.addDetails(postDetails);
                            post1.addComment(postComment);
                            post1.addComment(postComment1);
                            Author author =
                                    Author.builder()
                                            .email("user" + i + "@example.com")
                                            .firstName("firstName" + i)
                                            .lastName("lastName" + i)
                                            .build();
                            author.addPost(post);
                            author.addPost(post1);
                            this.authorRepository.save(author);
                        });
    }
}
