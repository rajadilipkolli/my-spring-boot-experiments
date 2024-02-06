package com.example.locks.services;

import com.example.locks.mapper.JpaLocksMapper;
import com.example.locks.model.response.ActorResponse;
import com.example.locks.repositories.ActorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActorService {

    private final JpaLocksMapper mapper;

    private final ActorRepository actorRepository;

    public Optional<ActorResponse> findActor(Long actorId){
        return actorRepository.findById(actorId).map(mapper::actorToActorResponse);
    }
}
