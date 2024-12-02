package com.example.keysetpagination.repositories;

import com.example.keysetpagination.entities.Animal;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.Predicate;
import java.util.List;
import java.util.Map;
import jdk.jfr.Registered;
import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.domain.Specification;

@Registered
public class CustomAnimalRepositoryImpl implements CustomAnimalRepository {

    private final EntityManager entityManager;

    public CustomAnimalRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Window<Animal> findAll(Specification<Animal> spec, PageRequest pageRequest, ScrollPosition scrollPosition) {
        // Build CriteriaQuery using the Specification
        var cb = entityManager.getCriteriaBuilder();
        var query = cb.createQuery(Animal.class);
        var root = query.from(Animal.class);

        Predicate filterPredicate = spec != null ? spec.toPredicate(root, query, cb) : cb.conjunction();

        // Handle ScrollPosition for keyset pagination
        if (scrollPosition instanceof KeysetScrollPosition keysetPosition) {
            Map<String, Object> keysetMap = keysetPosition.getKeys();
            for (Map.Entry<String, Object> entry : keysetMap.entrySet()) {
                String property = entry.getKey();
                Object value = entry.getValue();
                Predicate scrollPredicate = cb.greaterThan(root.get(property), (Comparable) value);
                filterPredicate = cb.and(filterPredicate, scrollPredicate);
            }
        }

        query.where(filterPredicate);

        // Apply sorting
        pageRequest.getSort().forEach(order -> {
            var path = root.get(order.getProperty());
            query.orderBy(order.isAscending() ? cb.asc(path) : cb.desc(path));
        });

        // Create the query and set paging parameters
        TypedQuery<Animal> typedQuery = entityManager.createQuery(query);
        typedQuery.setMaxResults(pageRequest.getPageSize() + 1); // Fetch one extra record

        // Execute the query and fetch results
        List<Animal> results = typedQuery.getResultList();

        // Determine if there are more results
        boolean hasMore = results.size() > pageRequest.getPageSize();

        // Trim the extra record if it exists
        if (hasMore) {
            results = results.subList(0, pageRequest.getPageSize());
        }

        // Generate next scroll position
        ScrollPosition nextScrollPosition = results.isEmpty()
                ? ScrollPosition.offset()
                : ScrollPosition.of(Map.of("id", results.getLast().getId()), ScrollPosition.Direction.FORWARD);

        // Return a new Window object with dynamic 'hasMore' value
        return new CustomWindow<>(results, nextScrollPosition, hasMore);
    }
}
