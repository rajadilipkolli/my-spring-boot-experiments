package com.example.keysetpagination.model.query;

import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class CriteriaGroup implements ISearchCriteria {

    private QueryOperator operator; // AND or OR
    private List<ISearchCriteria> criteriaList;

    public CriteriaGroup() {}

    public CriteriaGroup(QueryOperator operator, List<ISearchCriteria> criteriaList) {
        this.operator = operator;
        this.criteriaList = criteriaList;
    }

    public QueryOperator getOperator() {
        return operator;
    }

    public CriteriaGroup setOperator(QueryOperator operator) {
        this.operator = operator;
        return this;
    }

    public List<ISearchCriteria> getCriteriaList() {
        return criteriaList;
    }

    public CriteriaGroup setCriteriaList(List<ISearchCriteria> criteriaList) {
        this.criteriaList = criteriaList;
        return this;
    }

    @Override
    public Specification<?> toSpecification() {
        List<Specification> specs =
                criteriaList.stream().map(ISearchCriteria::toSpecification).toList();

        Specification result = specs.getFirst();
        for (int i = 1; i < specs.size(); i++) {
            if (getOperator() == QueryOperator.AND) {
                result = result.and(specs.get(i));
            } else if (getOperator() == QueryOperator.OR) {
                result = result.or(specs.get(i));
            } else {
                throw new UnsupportedOperationException("Operator not supported in group: " + operator);
            }
        }
        return result;
    }
}
