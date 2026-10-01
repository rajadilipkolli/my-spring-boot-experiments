package com.example.keysetpagination.model.query;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class CriteriaGroup<T> implements ISearchCriteria<T> {

    private LogicalOperator operator = LogicalOperator.AND;
    private List<ISearchCriteria<?>> criteriaList = new ArrayList<>();

    /** Creates an empty group using AND to combine its criteria. */
    public CriteriaGroup() {}

    /**
     * Creates a group with the supplied logical operator and child criteria.
     *
     * @param operator OR for alternatives; any other value uses AND
     * @param criteriaList child filters or groups; null creates an empty list
     */
    public CriteriaGroup(LogicalOperator operator, List<ISearchCriteria<?>> criteriaList) {
        this.operator = operator;
        this.criteriaList = criteriaList != null ? criteriaList : new ArrayList<>();
    }

    /** Returns the logical operator used to combine child criteria. */
    public LogicalOperator getOperator() {
        return operator;
    }

    /** Sets the logical operator; null is treated as AND when building the specification. */
    public void setOperator(LogicalOperator operator) {
        this.operator = operator;
    }

    /** Returns the mutable list of child filters and nested groups. */
    public List<ISearchCriteria<?>> getCriteriaList() {
        return criteriaList;
    }

    /** Replaces the child criteria; null is treated as an empty group during conversion. */
    public void setCriteriaList(List<ISearchCriteria<?>> criteriaList) {
        this.criteriaList = criteriaList;
    }

    /**
     * Combines non-null child criteria using this group's operator, including nested groups.
     *
     * @param entityClass entity type used to resolve filter attributes
     * @return the combined specification, contributing no restriction when no children contribute one
     */
    @Override
    @SuppressWarnings("unchecked")
    public Specification<T> toSpecification(Class<T> entityClass) {
        if (criteriaList == null || criteriaList.isEmpty()) {
            return Specification.where((Specification<T>) null);
        }

        List<Specification<T>> specs = new ArrayList<>();
        for (ISearchCriteria<?> criteria : criteriaList) {
            if (criteria != null) {
                specs.add(((ISearchCriteria<T>) criteria).toSpecification(entityClass));
            }
        }

        if (operator == LogicalOperator.OR) {
            return Specification.anyOf(specs);
        } else {
            return Specification.allOf(specs);
        }
    }
}
