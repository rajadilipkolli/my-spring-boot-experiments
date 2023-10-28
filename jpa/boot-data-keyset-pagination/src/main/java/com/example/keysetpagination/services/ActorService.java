package com.example.keysetpagination.services;

import com.blazebit.persistence.DefaultKeysetPage;
import com.blazebit.persistence.Keyset;
import com.blazebit.persistence.KeysetPage;
import com.blazebit.persistence.PagedList;
import com.example.keysetpagination.entities.Actor;
import com.example.keysetpagination.exception.ActorNotFoundException;
import com.example.keysetpagination.mapper.ActorMapper;
import com.example.keysetpagination.model.query.FindActorsQuery;
import com.example.keysetpagination.model.request.ActorRequest;
import com.example.keysetpagination.model.response.ActorResponse;
import com.example.keysetpagination.model.response.KeySetPageResponse;
import com.example.keysetpagination.model.response.PagedResult;
import com.example.keysetpagination.repositories.ActorRepository;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ActorService {

    private final ActorRepository actorRepository;
    private final ActorMapper actorMapper;

    public PagedResult<ActorResponse> firstLatestPosts(FindActorsQuery findActorsQuery) {
        Sort sort =
                Sort.by(
                        findActorsQuery.sortDir().equalsIgnoreCase(Sort.Direction.ASC.name())
                                ? Sort.Order.asc(findActorsQuery.sortBy())
                                : Sort.Order.desc(findActorsQuery.sortBy()));
        PagedList<Actor> topN = actorRepository.findTopN(sort, findActorsQuery.pageSize());
        return getActorResponsePagedResult(topN);
    }

    public PagedResult<ActorResponse> findNextLatestPosts(FindActorsQuery findActorsQuery) {
        Sort sort =
                Sort.by(
                        findActorsQuery.sortDir().equalsIgnoreCase(Sort.Direction.ASC.name())
                                ? Sort.Order.asc(findActorsQuery.sortBy())
                                : Sort.Order.desc(findActorsQuery.sortBy()));
        KeysetPage keysetPage = getKeysetPage(findActorsQuery);
        int firstPage = findActorsQuery.pageNo() * findActorsQuery.maxResults();
        PagedList<Actor> nextN =
                actorRepository.findNextN(
                        sort, keysetPage, firstPage, findActorsQuery.maxResults());
        return getActorResponsePagedResult(nextN);
    }

    private PagedResult<ActorResponse> getActorResponsePagedResult(PagedList<Actor> topN) {
        List<ActorResponse> actorResponseList = actorMapper.toResponseList(topN);

        KeysetPage keysetPage = topN.getKeysetPage();
        return new PagedResult<>(
                actorResponseList,
                topN.getTotalPages(),
                topN.getFirstResult(),
                topN.getPage(),
                topN.getSize(),
                topN.getTotalSize(),
                topN.getMaxResults(),
                new KeySetPageResponse(
                        keysetPage.getMaxResults(),
                        keysetPage.getFirstResult(),
                        Arrays.stream(keysetPage.getLowest().getTuple())
                                .map(String::valueOf)
                                .toList(),
                        Arrays.stream(keysetPage.getHighest().getTuple())
                                .map(String::valueOf)
                                .toList()));
    }

    private KeysetPage getKeysetPage(FindActorsQuery findActorsQuery) {
        Keyset lowestKeySet = () -> new Serializable[] {findActorsQuery.lowest()};
        Keyset highestKeySet = () -> new Serializable[] {findActorsQuery.lowest()};
        return new DefaultKeysetPage(
                findActorsQuery.firstResult(),
                findActorsQuery.maxResults(),
                lowestKeySet,
                highestKeySet);
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
