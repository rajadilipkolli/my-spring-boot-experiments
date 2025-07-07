package com.example.locks.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.example.locks.common.AbstractIntegrationTest;
import com.example.locks.entities.Actor;
import com.example.locks.model.request.ActorRequest;
import com.example.locks.model.response.ActorResponse;
import com.example.locks.repositories.ActorRepository;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

class ActorServiceIntTest extends AbstractIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(ActorServiceIntTest.class);

    @Autowired
    private ActorService actorService;

    @Autowired
    private ActorRepository actorRepository;

    @BeforeEach
    void setUp() {
        actorRepository.deleteAllInBatch();
    }

    @Test
    void testPessimisticWriteLock() {

        ActorResponse actorResponse = actorService.saveActor(new ActorRequest("Actor", null, "Indian"));

        Optional<Actor> optionalActor = actorRepository.findById(actorResponse.actorId());
        assertThat(optionalActor).isPresent();
        Actor actor = optionalActor.get();
        Short version = actor.getVersion();
        assertThat(actor.getActorName()).isEqualTo("Actor");
        assertThat(actor.getVersion()).isEqualTo((short) 0);

        final List<String> actorNames = List.of("PK", "MB");

        List<CompletableFuture<Void>> completableFutureList = actorNames.stream()
                .map(actorName -> CompletableFuture.runAsync(() -> {
                    try {
                        actorService.updateActorWithLock(actorResponse.actorId(), actorName);
                    } catch (Exception e) {
                        log.error("exception occurred", e);
                    }
                }))
                .toList();

        CompletableFuture.allOf(completableFutureList.toArray(CompletableFuture[]::new))
                .join();

        // Due to pessimistic lock exception is set for 5 sec only once will get updated, hence version should increment
        // only one
        optionalActor = actorRepository.findById(actorResponse.actorId());
        assertThat(optionalActor).isPresent();
        actor = optionalActor.get();
        assertThat(actor.getVersion()).isEqualTo((short) (version + 1));

        // As pessimistic lock is set for 5 sec after that it should update the value
        await().atMost(Duration.ofSeconds(10)).pollDelay(Duration.ofSeconds(1)).untilAsserted(() -> {
            Actor updatedActor = actorService.updateActorWithLock(actorResponse.actorId(), "newName");
            assertThat(updatedActor.getActorName()).isEqualTo("newName");
            assertThat(updatedActor.getVersion()).isEqualTo((short) 2);
        });
    }

    @Test
    void testPessimisticReadLock() throws ExecutionException, InterruptedException {
        ActorResponse actorResponse = actorService.saveActor(new ActorRequest("Actor", null, "Indian"));

        Optional<Actor> optionalActor = actorRepository.findById(actorResponse.actorId());
        assertThat(optionalActor).isPresent();
        Actor actor = optionalActor.get();
        assertThat(actor.getActorName()).isEqualTo("Actor");
        assertThat(actor.getVersion()).isEqualTo((short) 0);
        // Obtaining a pessimistic read lock concurrently by two requests on the same record
        List<CompletableFuture<Actor>> completableFutureList = IntStream.range(0, 2)
                .boxed()
                .map(actorName -> CompletableFuture.supplyAsync(
                        () -> actorService.getActorWithPessimisticReadLock(actorResponse.actorId())))
                .toList();

        CompletableFuture.allOf(completableFutureList.toArray(CompletableFuture[]::new))
                .join();
        // As pessimistic read lock is a shared lock it will give read access to every request
        assertThat(completableFutureList.getFirst().get().getActorName()).isEqualTo("Actor");
        assertThat(completableFutureList.get(1).get().getActorName()).isEqualTo("Actor");
    }

    @Test
    void testUpdatePessimisticReadLock() {
        ActorResponse actorResponse = actorService.saveActor(new ActorRequest("Actor", null, "Indian"));

        Optional<Actor> optionalActor = actorRepository.findById(actorResponse.actorId());
        assertThat(optionalActor).isPresent();
        Actor actor = optionalActor.get();
        assertThat(actor.getActorName()).isEqualTo("Actor");
        assertThat(actor.getVersion()).isEqualTo((short) 0);
        // Obtaining a pessimistic read lock and holding lock for 5 sec
        CompletableFuture.runAsync(() -> actorService.getActorWithPessimisticReadLock(actor.getActorId()));
        // As pessimistic read lock obtained on the record update can't be performed until the lock is released
        await().atMost(Duration.ofSeconds(10)).pollDelay(Duration.ofSeconds(1)).untilAsserted(() -> {
            ActorResponse updatedActor =
                    actorService.updateActor(actor.getActorId(), new ActorRequest("updateActor", null, "Indian"));
            assertThat(updatedActor.actorName()).isEqualTo("updateActor");
        });
    }
}
