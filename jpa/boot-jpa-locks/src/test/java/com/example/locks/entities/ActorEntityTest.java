package com.example.locks.entities;

import com.example.locks.repositories.ActorRepository;
import jakarta.persistence.PessimisticLockException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@DataJpaTest
public class ActorEntityTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ActorRepository actorRepository;

    @Test
    @Transactional
    public void testPessimisticWriteLock() {
        Actor entityToSave = new Actor();
        entityToSave.setActorName("TestEntityName");

        // When
//        Actor savedEntity = actorRepository.save(entityToSave);
        Actor savedEntity = entityManager.persistAndFlush(entityToSave);

        // Then
        assertNotNull(savedEntity.getActorId(), "Saved entity should have a non-null ID");
        assertEquals("Checking Data is saved", savedEntity.getActorName(), "TestEntityName");
        var savedActor = actorRepository.findById(1L).get();
        assertEquals("validating data is saved", savedActor.getActorName(), "TestEntityName");

        entityManager.getEntityManager().lock(savedEntity, PESSIMISTIC_WRITE);

        savedActor.setActorName("UpdatedEntityName");

//         Try to save the entity with the update
        assertThrows(PessimisticLockException.class, () -> {
            entityManager.persistAndFlush(savedActor);
            entityManager.flush();
        });


    }
}
