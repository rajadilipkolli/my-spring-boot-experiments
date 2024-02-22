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
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
class ActorServiceIntTest extends AbstractIntegrationTest {

    @Autowired
    private ActorService actorService;

    @Autowired
    private ActorRepository actorRepository;

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
        Short version = actor.getVersion();
        assertThat(actor.getActorName()).isEqualTo("Actor");
        assertThat(actor.getVersion()).isEqualTo((short) 0);
        // Obtaining a pessimistic read lock concurrently by two requests on the same record
        List<CompletableFuture<Actor>> completableFutureList = IntStream.range(0, 2)
                .boxed()
                .map(actorName -> CompletableFuture.supplyAsync(() -> {
                    var readLockActor = new Actor();
                    try {
                        readLockActor = actorService.getActorWithPessimisticReadLock(actorResponse.actorId());
                    } catch (Exception e) {
                        log.error("exception occurred", e);
                    }
                    return readLockActor;
                }))
                .toList();

        CompletableFuture.allOf(completableFutureList.toArray(CompletableFuture[]::new))
                .join();
        // As pessimistic read lock is a shared lock it will give read access to every request
        assertThat(completableFutureList.get(0).get().getActorName()).isEqualTo("Actor");
        assertThat(completableFutureList.get(1).get().getActorName()).isEqualTo("Actor");
    }
}
