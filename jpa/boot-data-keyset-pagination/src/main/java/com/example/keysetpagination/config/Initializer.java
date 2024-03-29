package com.example.keysetpagination.config;

import com.example.keysetpagination.entities.Actor;
import com.example.keysetpagination.repositories.ActorRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.LongStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class Initializer implements CommandLineRunner {

    private final ActorRepository actorRepository;

    @Override
    public void run(String... args) {
        log.info("Running Initializer.....");
        List<Actor> actorList =
                LongStream.rangeClosed(1, 50)
                        .mapToObj(
                                actorId -> {
                                    Actor actor =
                                            new Actor()
                                                    .setId(actorId)
                                                    .setName("Actor - %d".formatted(actorId));
                                    if (actorId % 2 == 0) {
                                        return actor.setCreatedOn(
                                                LocalDate.now().minusDays(actorId));
                                    } else {
                                        return actor.setCreatedOn(LocalDate.now());
                                    }
                                })
                        .toList();
        actorRepository.saveAll(actorList);
    }
}
