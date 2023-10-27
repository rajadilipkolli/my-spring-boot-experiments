package com.example.keysetpagination.services;

import com.example.keysetpagination.entities.Actor;
import com.example.keysetpagination.exception.ActorNotFoundException;
import com.example.keysetpagination.mapper.ActorMapper;
import com.example.keysetpagination.model.query.FindActorsQuery;
import com.example.keysetpagination.model.request.ActorRequest;
import com.example.keysetpagination.model.response.ActorResponse;
import com.example.keysetpagination.model.response.PagedResult;
import com.example.keysetpagination.repositories.ActorRepository;
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
@Transactional
@RequiredArgsConstructor
public class ActorService {

    private final ActorRepository actorRepository;
    private final ActorMapper actorMapper;

    public PagedResult<ActorResponse> findAllActors(FindActorsQuery findActorsQuery) {

        // create Pageable instance
        Pageable pageable = createPageable(findActorsQuery);

        Page<Actor> actorsPage = actorRepository.findAll(pageable);

        List<ActorResponse> actorResponses = actorMapper.toResponseList(actorsPage.getContent());

        return new PagedResult<>(
                actorResponses,
                actorsPage.getTotalElements(),
                actorsPage.getNumber() + 1,
                actorsPage.getTotalPages(),
                actorsPage.isFirst(),
                actorsPage.isLast(),
                actorsPage.hasNext(),
                actorsPage.hasPrevious());
    }

    private Pageable createPageable(FindActorsQuery findActorsQuery) {
        int pageNo = Math.max(findActorsQuery.pageNo() - 1, 0);
        Sort sort =
                Sort.by(
                        findActorsQuery.sortDir().equalsIgnoreCase(Sort.Direction.ASC.name())
                                ? Sort.Order.asc(findActorsQuery.sortBy())
                                : Sort.Order.desc(findActorsQuery.sortBy()));
        return PageRequest.of(pageNo, findActorsQuery.pageSize(), sort);
    }

    public Optional<ActorResponse> findActorById(Long id) {
        return actorRepository.findById(id).map(actorMapper::toResponse);
    }

    public ActorResponse saveActor(ActorRequest actorRequest) {
        Actor actor = actorMapper.toEntity(actorRequest);
        Actor savedActor = actorRepository.save(actor);
        return actorMapper.toResponse(savedActor);
    }

    public ActorResponse updateActor(Long id, ActorRequest actorRequest) {
        Actor actor =
                actorRepository.findById(id).orElseThrow(() -> new ActorNotFoundException(id));

        // Update the actor object with data from actorRequest
        actorMapper.mapActorWithRequest(actor, actorRequest);

        // Save the updated actor object
        Actor updatedActor = actorRepository.save(actor);

        return actorMapper.toResponse(updatedActor);
    }

    public void deleteActorById(Long id) {
        actorRepository.deleteById(id);
    }
}
