package com.example.locks.entities;

import com.example.locks.common.AbstractIntegrationTest;
import com.example.locks.repositories.ActorRepository;
import com.example.locks.services.ActorService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@Slf4j
public class ActorEntityTest extends AbstractIntegrationTest {

    @Autowired
    private ActorRepository actorRepository;

    @Autowired
    private ActorService actorService;

    @Test
    public void testPessimisticWriteLock() throws InterruptedException {

        final Actor srcItem = actorRepository.save(new Actor().setActorId(1L).setActorName("Actor"));
        var findData = actorRepository.findById(srcItem.getActorId);
        assertEquals("Data Saved", "Actor", findData.orElse(new Actor()).getActorName());

        final List<String> actorNames = Arrays.asList("PK", "MB");
        final ExecutorService executor = Executors.newFixedThreadPool(actorNames.size());


        for (String actor : actorNames) {
            executor.execute(() -> actorService.updateActorWithLock(srcItem.getActorId(), actor));
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(1, TimeUnit.MINUTES));

    }

}
