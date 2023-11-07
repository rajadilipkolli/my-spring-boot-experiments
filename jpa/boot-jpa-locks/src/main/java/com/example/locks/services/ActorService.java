package com.example.locks.services;

import com.example.locks.entities.Actor;
import com.example.locks.exception.ActorNotFoundException;
import com.example.locks.mapper.ActorMapper;
import com.example.locks.model.query.FindActorsQuery;
import com.example.locks.model.request.ActorRequest;
import com.example.locks.model.response.ActorResponse;
import com.example.locks.model.response.PagedResult;
import com.example.locks.repositories.ActorRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ActorService {

    private final ActorRepository actorRepository;
    private final ActorMapper actorMapper;

    public PagedResult<ActorResponse> findAllActors(FindActorsQuery findActorsQuery) {

        // create Pageable instance
        Pageable pageable = createPageable(findActorsQuery);

        Page<Actor> actorsPage = actorRepository.findAll(pageable);

        List<ActorResponse> actorResponseList = actorMapper.toResponseList(actorsPage.getContent());

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

    public Optional<ActorResponse> findActorById(Long id) {
        return actorRepository.findById(id).map(actorMapper::toResponse);
    }

    @Transactional
    public ActorResponse saveActor(ActorRequest actorRequest) {
        Actor actor = actorMapper.toEntity(actorRequest);
        Actor savedActor = actorRepository.save(actor);
        return actorMapper.toResponse(savedActor);
    }

    @Transactional
    public ActorResponse updateActor(Long id, ActorRequest actorRequest) {
        Actor actor = actorRepository.findById(id).orElseThrow(() -> new ActorNotFoundException(id));

        // Update the actor object with data from actorRequest
        actorMapper.mapActorWithRequest(actor, actorRequest);

        // Save the updated actor object
        Actor updatedActor = actorRepository.save(actor);

        return actorMapper.toResponse(updatedActor);
    }

    @Transactional
    public void deleteActorById(Long id) {
        actorRepository.deleteById(id);
    }
}
