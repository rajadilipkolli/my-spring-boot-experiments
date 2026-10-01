package com.example.keysetpagination.model.query;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.data.jpa.domain.Specification;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type",
        defaultImpl = SearchCriteria.class)
@JsonSubTypes({
    @JsonSubTypes.Type(value = SearchCriteria.class, name = "criteria"),
    @JsonSubTypes.Type(value = CriteriaGroup.class, name = "group")
})
public interface ISearchCriteria<T> {
    /**
     * Converts this filter or group into a specification for the target entity.
     *
     * @param entityClass entity type whose attributes the criteria reference
     * @return a specification representing the filter or logical group
     */
    Specification<T> toSpecification(Class<T> entityClass);
}
