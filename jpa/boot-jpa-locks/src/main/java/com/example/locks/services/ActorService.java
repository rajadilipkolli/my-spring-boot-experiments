package com.example.locks.services;

import com.example.locks.entities.Actor;
import com.example.locks.exception.ActorNotFoundException;
import com.example.locks.mapper.JpaLocksMapper;
import com.example.locks.model.query.FindActorsQuery;
import com.example.locks.model.request.ActorRequest;
import com.example.locks.model.response.ActorResponse;
import com.example.locks.model.response.PagedResult;
import com.example.locks.repositories.ActorRepository;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActorService {

    private static final Logger log = LoggerFactory.getLogger(ActorService.class);

    private final JpaLocksMapper mapper;
    private final ActorRepository actorRepository;

    public ActorService(JpaLocksMapper mapper, ActorRepository actorRepository) {
        this.mapper = mapper;
        this.actorRepository = actorRepository;
    }

    public PagedResult<ActorResponse> findAllActors(FindActorsQuery findActorsQuery) {

        // create Pageable instance
        Pageable pageable = createPageable(findActorsQuery);

        Page<Actor> actorsPage = actorRepository.findAll(pageable);

        List<ActorResponse> actorResponseList = mapper.actorToActorResponseList(actorsPage.getContent());

        return new PagedResult<>(actorsPage, actorResponseList);
    }

    private Pageable createPageable(FindActorsQuery findActorsQuery) {
        int pageNo = Math.max(findActorsQuery.pageNo() - 1, 0);
        Sort sort = Sort.by(
                findActorsQuery.sortDir().equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.Order.asc(findActorsQuery.sortBy())
                        : Sort.Order.desc(findActorsQuery.sortBy()));
        return PageRequest.of(pageNo, findActorsQuery.pageSize(), sort);
    }

    public Optional<ActorResponse> findActorById(Long actorId) {
        return actorRepository.findById(actorId).map(mapper::actorToActorResponse);
    }

    @Transactional
    public ActorResponse saveActor(ActorRequest actorRequest) {
        Actor actor = mapper.toActorEntity(actorRequest);
        Actor savedActor = actorRepository.save(actor);
        return mapper.actorToActorResponse(savedActor);
    }

    @Transactional
    public ActorResponse updateActor(Long id, ActorRequest actorRequest) {
        Actor actor = actorRepository.findById(id).orElseThrow(() -> new ActorNotFoundException(id));

        // Update the actor object with data from actorRequest
        mapper.mapActorWithRequest(actorRequest, actor);

        // Save the updated actor object
        Actor updatedActor = actorRepository.save(actor);

        return mapper.actorToActorResponse(updatedActor);
    }

    @Transactional
    public void deleteActorById(Long id) {
        actorRepository.deleteById(id);
    }

    @Transactional
    public Actor updateActorWithLock(Long id, String name) {
        try {
            return obtainPessimisticLockAndUpdate(id, name);
        } catch (PessimisticLockingFailureException e) {
            log.info("Received exception for request {}", name);
            log.error("Found pessimistic lock exception!", e);
            sleepForAWhile();
        }
        return null;
    }

    public Actor obtainPessimisticLockAndUpdate(Long id, String name) {
        Optional<Actor> actor = actorRepository.findByIdWithPessimisticWriteLock(id);
        actor.ifPresent(obj -> {
            sleepForAWhile();
            obj.setActorName(name);
            actorRepository.save(obj);
        });
        return actor.orElseThrow(() -> new ActorNotFoundException(id));
    }

    private void sleepForAWhile() {
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    @Transactional
    public Actor getActorWithPessimisticReadLock(Long id) {
        return actorRepository.findByIdWithPessimisticReadLock(id).orElseThrow(() -> new ActorNotFoundException(id));
    }
}
