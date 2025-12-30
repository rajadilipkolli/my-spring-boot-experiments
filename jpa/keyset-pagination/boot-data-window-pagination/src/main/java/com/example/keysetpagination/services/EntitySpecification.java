package com.example.keysetpagination.services;

import com.example.keysetpagination.model.query.CriteriaGroup;
import com.example.keysetpagination.model.query.ISearchCriteria;
import com.example.keysetpagination.model.query.SearchCriteria;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.Metamodel;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class EntitySpecification<T> {

    private final EntityManager entityManager;

    public EntitySpecification(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Specification specificationBuilder(List<ISearchCriteria> searchCriteriaList, Class<T> entityClass) {
        validateMetadata(searchCriteriaList, entityClass);
        Specification result = Specification.unrestricted();
        for (ISearchCriteria criteria : searchCriteriaList) {
            result = result.and(criteria.toSpecification());
        }
        return result;
    }

    private void validateMetadata(List<ISearchCriteria> searchCriteriaList, Class<T> entityClass) {
        Metamodel metamodel = entityManager.getMetamodel();
        ManagedType<T> managedType = metamodel.managedType(entityClass);

        for (ISearchCriteria<?> isearchCriteria : searchCriteriaList) {
            processSearchCriteria(isearchCriteria, managedType);
        }
    }

    private void processSearchCriteria(ISearchCriteria<?> isearchCriteria, ManagedType<T> managedType) {
        if (isearchCriteria instanceof SearchCriteria<?> searchCriteria) {
            validateSearchCriteria(searchCriteria, managedType);
        } else if (isearchCriteria instanceof CriteriaGroup group) {
            for (ISearchCriteria<?> criteria : group.getCriteriaList()) {
                processSearchCriteria(criteria, managedType);
            }
        }
    }

    private void validateSearchCriteria(SearchCriteria<?> searchCriteria, ManagedType<T> managedType) {
        String fieldName = searchCriteria.getField();
        if (managedType.getAttribute(fieldName) == null) {
            throw new IllegalArgumentException("Invalid field: " + fieldName);
        }
    }
}
