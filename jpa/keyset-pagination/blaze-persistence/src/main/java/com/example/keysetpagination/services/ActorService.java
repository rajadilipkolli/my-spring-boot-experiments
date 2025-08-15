package com.example.keysetpagination.services;

import com.blazebit.persistence.DefaultKeysetPage;
import com.blazebit.persistence.KeysetPage;
import com.blazebit.persistence.spring.data.repository.KeysetAwarePage;
import com.blazebit.persistence.spring.data.repository.KeysetPageRequest;
import com.blazebit.persistence.spring.data.repository.KeysetPageable;
import com.example.keysetpagination.entities.Actor;
import com.example.keysetpagination.exception.ActorNotFoundException;
import com.example.keysetpagination.mapper.ActorMapper;
import com.example.keysetpagination.model.query.FindActorsQuery;
import com.example.keysetpagination.model.query.SearchCriteria;
import com.example.keysetpagination.model.request.ActorRequest;
import com.example.keysetpagination.model.response.ActorResponse;
import com.example.keysetpagination.model.response.PagedResult;
import com.example.keysetpagination.repositories.ActorRepository;
import com.example.keysetpagination.utils.EntitySpecification;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ActorService {

    private final ActorRepository actorRepository;
    private final ActorMapper actorMapper;
    private final EntitySpecification<Actor> actorEntitySpecification;

    public ActorService(ActorRepository actorRepository, ActorMapper actorMapper) {
        this.actorRepository = actorRepository;
        this.actorMapper = actorMapper;
        actorEntitySpecification = new EntitySpecification<>();
    }

    public PagedResult<ActorResponse> findAll(SearchCriteria[] searchCriteria, FindActorsQuery findActorsQuery) {
        Specification<Actor> specification = actorEntitySpecification.specificationBuilder(searchCriteria, Actor.class);
        KeysetPageable keysetPageable = createPageable(findActorsQuery);
        return getActorResponsePagedResult(actorRepository.findAll(specification, keysetPageable));
    }

    public PagedResult<ActorResponse> findAll(FindActorsQuery findActorsQuery) {
        KeysetPageable keysetPageable = createPageable(findActorsQuery);
        return getActorResponsePagedResult(actorRepository.findAll(keysetPageable));
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

    private PagedResult<ActorResponse> getActorResponsePagedResult(KeysetAwarePage<Actor> keysetAwarePage) {
        List<ActorResponse> actorResponseList = actorMapper.toResponseList(keysetAwarePage.getContent());
        PagedResult<Actor> actorPagedResult = new PagedResult<>(keysetAwarePage);

        return actorPagedResult.toResponseRecord(actorResponseList);
    }

    private KeysetPageable createPageable(FindActorsQuery findActorsQuery) {
        int pageNo = Math.max(findActorsQuery.pageNo() - 1, 0);
        Sort sort = Sort.by(
                findActorsQuery.sortDir().equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.Order.asc(findActorsQuery.sortBy())
                        : Sort.Order.desc(findActorsQuery.sortBy()));
        KeysetPage keysetPage = new DefaultKeysetPage(
                findActorsQuery.pageNo() - 1,
                findActorsQuery.pageSize(),
                () -> new Serializable[] {findActorsQuery.lowest()},
                () -> new Serializable[] {findActorsQuery.highest()});
        return new KeysetPageRequest(keysetPage, PageRequest.of(pageNo, findActorsQuery.pageSize(), sort));
    }
}
