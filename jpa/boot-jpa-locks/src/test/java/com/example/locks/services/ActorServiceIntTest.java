package com.example.locks.services;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.locks.common.AbstractIntegrationTest;
import com.example.locks.model.request.ActorRequest;
import com.example.locks.model.response.ActorResponse;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ActorServiceIntTest extends AbstractIntegrationTest {

    @Autowired
    private ActorService actorService;

    @Test
    public void testPessimisticWriteLock() throws InterruptedException {

        ActorResponse actorResponse = actorService.saveActor(new ActorRequest("Actor", null, "Indian"));

        var findData = actorService.findActorById(actorResponse.actorId());
        assertThat(findData).isPresent();
        assertThat(findData.get().actorName()).isEqualTo("Actor");

        final List<String> actorNames = Arrays.asList("PK", "MB");
        final ExecutorService executor = Executors.newFixedThreadPool(actorNames.size());

        for (String actor : actorNames) {
            executor.execute(
                    () -> actorService.updateActorWithLock(findData.get().actorId(), actor));
        }

        executor.shutdown();
        assertThat(executor.awaitTermination(1, TimeUnit.MINUTES)).isTrue();
    }
}
