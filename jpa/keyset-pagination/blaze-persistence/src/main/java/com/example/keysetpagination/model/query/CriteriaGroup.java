package com.example.keysetpagination.model.query;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class CriteriaGroup<T> implements ISearchCriteria<T> {

    private LogicalOperator operator = LogicalOperator.AND;
    private List<ISearchCriteria<?>> criteriaList = new ArrayList<>();

    public CriteriaGroup() {}

    public CriteriaGroup(LogicalOperator operator, List<ISearchCriteria<?>> criteriaList) {
        this.operator = operator;
        this.criteriaList = criteriaList != null ? criteriaList : new ArrayList<>();
    }

    public LogicalOperator getOperator() {
        return operator;
    }

    public void setOperator(LogicalOperator operator) {
        this.operator = operator;
    }

    public List<ISearchCriteria<?>> getCriteriaList() {
        return criteriaList;
    }

    public void setCriteriaList(List<ISearchCriteria<?>> criteriaList) {
        this.criteriaList = criteriaList;
    }

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
