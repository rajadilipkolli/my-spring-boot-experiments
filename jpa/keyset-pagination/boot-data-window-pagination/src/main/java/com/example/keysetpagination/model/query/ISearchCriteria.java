package com.example.keysetpagination.model.query;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.data.jpa.domain.Specification;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type",
        defaultImpl = SearchCriteria.class // Set default implementation
        )
@JsonSubTypes({
    @JsonSubTypes.Type(value = SearchCriteria.class, name = "criteria"),
    @JsonSubTypes.Type(value = CriteriaGroup.class, name = "group")
})
public interface ISearchCriteria<T> {

    Specification<T> toSpecification();
}
