package com.example.keysetpagination.services;

import com.blazebit.persistence.DefaultKeysetPage;
import com.blazebit.persistence.KeysetPage;
import com.blazebit.persistence.spring.data.repository.KeysetAwarePage;
import com.blazebit.persistence.spring.data.repository.KeysetPageRequest;
import com.blazebit.persistence.spring.data.repository.KeysetPageable;
import com.blazebit.text.FormatUtils;
import com.blazebit.text.ParserContext;
import com.blazebit.text.SerializableFormat;
import com.example.keysetpagination.entities.Actor;
import com.example.keysetpagination.exception.ActorNotFoundException;
import com.example.keysetpagination.mapper.ActorMapper;
import com.example.keysetpagination.model.query.ActorsFilter;
import com.example.keysetpagination.model.query.FindActorsQuery;
import com.example.keysetpagination.model.request.ActorRequest;
import com.example.keysetpagination.model.response.ActorResponse;
import com.example.keysetpagination.model.response.KeySetPageResponse;
import com.example.keysetpagination.model.response.PagedResult;
import com.example.keysetpagination.repositories.ActorRepository;
import jakarta.persistence.criteria.*;
import java.io.Serializable;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ActorService {

    private final ActorRepository actorRepository;
    private final ActorMapper actorMapper;

    public PagedResult<ActorResponse> findAll(
            ActorsFilter[] actorsFilter, FindActorsQuery findActorsQuery) {
        Specification<Actor> specification = getSpecificationForFilter(actorsFilter);
        KeysetPageable keysetPageable = createPageable(findActorsQuery);
        KeysetAwarePage<Actor> keysetAwarePage =
                actorRepository.findAll(specification, keysetPageable);
        List<ActorResponse> actorResponseList =
                actorMapper.toResponseList(keysetAwarePage.getContent());
        return new PagedResult<>(
                actorResponseList,
                keysetAwarePage.getTotalElements(),
                keysetAwarePage.getNumber() + 1,
                keysetAwarePage.getTotalPages(),
                keysetAwarePage.isFirst(),
                keysetAwarePage.isLast(),
                keysetAwarePage.hasNext(),
                keysetAwarePage.hasPrevious(),
                new KeySetPageResponse(
                        keysetAwarePage.getKeysetPage().getMaxResults(),
                        keysetAwarePage.getKeysetPage().getFirstResult(),
                        (Long) keysetAwarePage.getKeysetPage().getLowest().getTuple()[0],
                        (Long) keysetAwarePage.getKeysetPage().getHighest().getTuple()[0]));
    }

    private KeysetPageable createPageable(FindActorsQuery findActorsQuery) {
        int pageNo = Math.max(findActorsQuery.pageNo() - 1, 0);
        Sort sort =
                Sort.by(
                        findActorsQuery.sortDir().equalsIgnoreCase(Sort.Direction.ASC.name())
                                ? Sort.Order.asc(findActorsQuery.sortBy())
                                : Sort.Order.desc(findActorsQuery.sortBy()));
        KeysetPage keysetPage =
                new DefaultKeysetPage(
                        findActorsQuery.firstResult() == null ? 0 : findActorsQuery.firstResult(),
                        findActorsQuery.maxResults() == null
                                ? findActorsQuery.pageSize()
                                : findActorsQuery.maxResults(),
                        () -> new Serializable[] {findActorsQuery.lowest()},
                        () -> new Serializable[] {findActorsQuery.highest()});
        return new KeysetPageRequest(
                keysetPage, PageRequest.of(pageNo, findActorsQuery.pageSize(), sort));
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

    private Specification<Actor> getSpecificationForFilter(final ActorsFilter[] actorsFilter) {
        if (actorsFilter == null || actorsFilter.length == 0) {
            return null;
        }
        return new Specification<Actor>() {
            @Override
            public Predicate toPredicate(
                    Root<Actor> root,
                    CriteriaQuery<?> criteriaQuery,
                    CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                ParserContext parserContext = new ParserContextImpl();
                try {
                    for (ActorsFilter f : actorsFilter) {
                        SerializableFormat<?> format = FILTER_ATTRIBUTES.get(f.getField());
                        if (format != null) {
                            String[] fieldParts = f.getField().split("\\.");
                            Path<?> path = root.get(fieldParts[0]);
                            for (int i = 1; i < fieldParts.length; i++) {
                                path = path.get(fieldParts[i]);
                            }
                            switch (f.getKind()) {
                                case EQ:
                                    predicates.add(
                                            criteriaBuilder.equal(
                                                    path,
                                                    format.parse(f.getValue(), parserContext)));
                                    break;
                                case GT:
                                    predicates.add(
                                            criteriaBuilder.greaterThan(
                                                    (Expression<Comparable>) path,
                                                    (Comparable)
                                                            format.parse(
                                                                    f.getValue(), parserContext)));
                                    break;
                                case LT:
                                    predicates.add(
                                            criteriaBuilder.lessThan(
                                                    (Expression<Comparable>) path,
                                                    (Comparable)
                                                            format.parse(
                                                                    f.getValue(), parserContext)));
                                    break;
                                case GTE:
                                    predicates.add(
                                            criteriaBuilder.greaterThanOrEqualTo(
                                                    (Expression<Comparable>) path,
                                                    (Comparable)
                                                            format.parse(
                                                                    f.getValue(), parserContext)));
                                    break;
                                case LTE:
                                    predicates.add(
                                            criteriaBuilder.lessThanOrEqualTo(
                                                    (Expression<Comparable>) path,
                                                    (Comparable)
                                                            format.parse(
                                                                    f.getValue(), parserContext)));
                                    break;
                                case IN:
                                    List<String> values = f.getValues();
                                    List<Object> filterValues = new ArrayList<>(values.size());
                                    for (String value : values) {
                                        filterValues.add(format.parse(value, parserContext));
                                    }
                                    predicates.add(path.in(filterValues));
                                    break;
                                case BETWEEN:
                                    predicates.add(
                                            criteriaBuilder.between(
                                                    (Expression<Comparable>) path,
                                                    (Comparable)
                                                            format.parse(f.getLow(), parserContext),
                                                    (Comparable)
                                                            format.parse(
                                                                    f.getHigh(), parserContext)));
                                    break;
                                case STARTS_WITH:
                                    predicates.add(
                                            criteriaBuilder.like(
                                                    (Expression<String>) path,
                                                    format.parse(f.getValue(), parserContext)
                                                            + "%"));
                                    break;
                                case ENDS_WITH:
                                    predicates.add(
                                            criteriaBuilder.like(
                                                    (Expression<String>) path,
                                                    "%"
                                                            + format.parse(
                                                                    f.getValue(), parserContext)));
                                    break;
                                case CONTAINS:
                                    predicates.add(
                                            criteriaBuilder.like(
                                                    (Expression<String>) path,
                                                    "%"
                                                            + format.parse(
                                                                    f.getValue(), parserContext)
                                                            + "%"));
                                    break;
                                default:
                                    throw new UnsupportedOperationException(
                                            "Unsupported kind: " + f.getKind());
                            }
                        }
                    }
                } catch (ParseException ex) {
                    throw new RuntimeException(ex);
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
    }

    private static class ParserContextImpl implements ParserContext {
        private final Map<String, Object> contextMap;

        private ParserContextImpl() {
            this.contextMap = new HashMap();
        }

        public Object getAttribute(String name) {
            return this.contextMap.get(name);
        }

        public void setAttribute(String name, Object value) {
            this.contextMap.put(name, value);
        }
    }

    private static final Map<String, SerializableFormat<?>> FILTER_ATTRIBUTES;

    static {
        Map<String, SerializableFormat<?>> filterAttributes = new HashMap<>();
        filterAttributes.put("id", FormatUtils.getAvailableFormatters().get(Long.class));
        filterAttributes.put("text", FormatUtils.getAvailableFormatters().get(String.class));
        filterAttributes.put(
                "createdOn", FormatUtils.getAvailableFormatters().get(LocalDate.class));
        FILTER_ATTRIBUTES = Collections.unmodifiableMap(filterAttributes);
    }
}
