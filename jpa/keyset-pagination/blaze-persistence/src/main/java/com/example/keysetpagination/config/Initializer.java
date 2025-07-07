package com.example.keysetpagination.config;

import com.example.keysetpagination.entities.Actor;
import com.example.keysetpagination.repositories.ActorRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.LongStream;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
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
                                            new Actor().setName("Actor - %d".formatted(actorId));
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
