package com.example.locks.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.example.locks.common.AbstractIntegrationTest;
import com.example.locks.model.request.ActorRequest;
import com.example.locks.model.response.ActorResponse;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ActorServiceIntTest extends AbstractIntegrationTest {

    @Autowired
    private ActorService actorService;

    @Test
    void testPessimisticWriteLock() throws InterruptedException {

        ActorResponse actorResponse = actorService.saveActor(new ActorRequest("Actor", null, "Indian"));

        var optionalActorResponse = actorService.findActorById(actorResponse.actorId());
        assertThat(optionalActorResponse).isPresent();
        ActorResponse actorResponse1 = optionalActorResponse.get();
        assertThat(actorResponse1.actorName()).isEqualTo("Actor");

        final List<String> actorNames = new LinkedList<>();
        actorNames.add("PK");
        actorNames.add("MB");
        final ExecutorService executor = Executors.newFixedThreadPool(actorNames.size());

        for (int i = 0; i <= actorNames.size(); i++) {
            int finalI = i;
            executor.execute(() -> actorService.updateActorWithLock(actorResponse1.actorId(), actorNames.get(finalI)));
        }

        executor.shutdown();
        assertThat(executor.awaitTermination(1, TimeUnit.MINUTES)).isTrue();
        // Due to pessimistic lock exception None got updated
        optionalActorResponse = actorService.findActorById(actorResponse.actorId());
        assertThat(actorResponse1.actorName()).isEqualTo("Actor");

        // As pessimistic lock is set for 5 sec after that it should update the value
        await().atMost(Duration.ofSeconds(10)).pollDelay(Duration.ofSeconds(2)).untilAsserted(() -> {
            actorService.updateActorWithLock(actorResponse1.actorId(), actorNames.getFirst());
            Optional<ActorResponse> response = actorService.findActorById(actorResponse.actorId());
            assertThat(response).isPresent();
            assertThat(response.get().actorName()).isEqualTo("PK");
        });
    }
}
