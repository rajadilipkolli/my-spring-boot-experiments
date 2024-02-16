package com.example.locks.repositories;

import com.example.locks.entities.Actor;

public interface CustomizedActorRepository {

    long getLockTimeout();

    void setLockTimeout(long timeoutDurationInMs);

    Actor getActorAndObtainPessimisticWriteLockingOnItById(Long id);
}
