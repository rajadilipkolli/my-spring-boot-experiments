package com.example.graphql.config;

import com.example.graphql.entities.Author;
import com.example.graphql.repositories.AuthorRepository;
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
                            Author author = new Author(i, "user", "user@example.com");
                            this.authorRepository.save(author);
                        });
    }
}
