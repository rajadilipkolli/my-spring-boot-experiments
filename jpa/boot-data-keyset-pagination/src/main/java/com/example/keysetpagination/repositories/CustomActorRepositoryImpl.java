package com.example.keysetpagination.repositories;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.KeysetPage;
import com.blazebit.persistence.PagedList;
import com.example.keysetpagination.entities.Actor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

public class CustomActorRepositoryImpl implements CustomActorRepository {

    @PersistenceContext private EntityManager entityManager;

    @Autowired private CriteriaBuilderFactory criteriaBuilderFactory;

    @Override
    public PagedList<Actor> findTopN(Sort sortBy, int pageSize) {
        return sortedCriteriaBuilder(sortBy)
                .page(0, pageSize)
                .withKeysetExtraction(true)
                .getResultList();
    }

    @Override
    public PagedList<Actor> findNextN(
            Sort sortBy, KeysetPage keysetPage, int firstResult, int maxResult) {
        return sortedCriteriaBuilder(sortBy)
                .page(keysetPage, firstResult, maxResult)
                .getResultList();
    }

    private CriteriaBuilder<Actor> sortedCriteriaBuilder(Sort sortBy) {
        CriteriaBuilder<Actor> criteriaBuilder =
                criteriaBuilderFactory.create(entityManager, Actor.class);

        sortBy.forEach(
                order -> {
                    criteriaBuilder.orderBy(order.getProperty(), order.isAscending());
                });

        return criteriaBuilder;
    }
}
