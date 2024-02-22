package com.example.locks.repositories;

import com.example.locks.entities.Actor;
import jakarta.persistence.LockModeType;

public interface CustomizedActorRepository {

    long getLockTimeout();

    void setLockTimeout(long timeoutDurationInMs);

    Actor getActorAndObtainPessimisticLockingOnItById(Long id, LockModeType lockModeType);
}
