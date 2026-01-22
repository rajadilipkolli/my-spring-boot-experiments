package com.example.keysetpagination.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
public class CustomRepositoryImpl<T> implements CustomRepository<T> {

    private final EntityManager entityManager;

    public CustomRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Window<@NonNull T> findAll(
            Specification<@NonNull T> spec,
            PageRequest pageRequest,
            ScrollPosition scrollPosition,
            Class<T> entityClass) {

        // Build CriteriaQuery using Specification
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);

        Predicate filterPredicate = spec != null ? spec.toPredicate(root, query, cb) : cb.conjunction();

        // Determine scroll direction and apply keyset filters if necessary
        ScrollPosition.Direction direction = ScrollPosition.Direction.FORWARD;
        if (scrollPosition instanceof KeysetScrollPosition keysetPosition) {
            direction = keysetPosition.getDirection();
            filterPredicate = applyKeySetFilter(keysetPosition, root, cb, filterPredicate, direction);
        }

        query.where(filterPredicate);

        // Apply sorting
        applySorting(query, root, cb, pageRequest);

        // Create and execute query
        TypedQuery<T> typedQuery = entityManager.createQuery(query);
        typedQuery.setMaxResults(pageRequest.getPageSize() + 1); // Fetch one extra record

        List<T> results = typedQuery.getResultList();

        // Determine if there are more results
        boolean hasMore = results.size() > pageRequest.getPageSize();

        // Trim the extra record if it exists
        if (hasMore) {
            results = results.subList(0, pageRequest.getPageSize());
        }

        // Generate next scroll position
        ScrollPosition nextScrollPosition = generateNextScrollPosition(results, direction);

        // Return a new Window object
        return new CustomWindow<>(results, nextScrollPosition, hasMore);
    }

    private Predicate applyKeySetFilter(
            KeysetScrollPosition scrollPosition,
            Root<T> root,
            CriteriaBuilder cb,
            Predicate currentPredicate,
            ScrollPosition.Direction direction) {

        Map<String, Object> keysetMap = scrollPosition.getKeys();
        for (Map.Entry<String, Object> entry : keysetMap.entrySet()) {
            String property = entry.getKey();
            Object value = entry.getValue();
            Predicate scrollPredicate;

            if (direction == ScrollPosition.Direction.FORWARD) {
                scrollPredicate = cb.greaterThan(root.get(property), (Comparable) value);
            } else {
                scrollPredicate = cb.lessThan(root.get(property), (Comparable) value);
            }

            currentPredicate = cb.and(currentPredicate, scrollPredicate);
        }
        return currentPredicate;
    }

    private void applySorting(CriteriaQuery<T> query, Root<T> root, CriteriaBuilder cb, PageRequest pageRequest) {

        List<Order> orders = new ArrayList<>();
        pageRequest.getSort().forEach(order -> {
            Path<?> path = root.get(order.getProperty());
            orders.add(order.isAscending() ? cb.asc(path) : cb.desc(path));
        });

        if (!orders.isEmpty()) {
            query.orderBy(orders);
        }
    }

    private ScrollPosition generateNextScrollPosition(List<T> results, ScrollPosition.Direction direction) {

        if (results.isEmpty()) {
            return ScrollPosition.offset();
        }

        // Assuming 'id' is used for keyset pagination; customize as needed
        Object lastId =
                entityManager.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(results.getLast());
        return ScrollPosition.of(Map.of("id", lastId), direction);
    }
}
