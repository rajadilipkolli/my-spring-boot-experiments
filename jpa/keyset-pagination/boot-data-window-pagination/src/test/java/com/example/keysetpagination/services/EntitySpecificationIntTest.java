package com.example.keysetpagination.services;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.keysetpagination.common.AbstractIntegrationTest;
import com.example.keysetpagination.entities.Animal;
import com.example.keysetpagination.model.query.QueryOperator;
import com.example.keysetpagination.model.query.SearchCriteria;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

class EntitySpecificationIntTest extends AbstractIntegrationTest {

    private EntitySpecification<Animal> entitySpecification;

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        entitySpecification = new EntitySpecification<>(entityManager);
    }

    @Test
    void shouldBuildSpecificationForEQOperator() {
        SearchCriteria criteria = new SearchCriteria(QueryOperator.EQ, "type", List.of("Mammal"));
        Specification<Animal> spec = entitySpecification.specificationBuilder(List.of(criteria), Animal.class);
        assertThat(spec).isNotNull();
    }

    @Test
    void shouldBuildSpecificationForNEOperator() {
        SearchCriteria criteria = new SearchCriteria(QueryOperator.NE, "type", List.of("Reptile"));
        Specification<Animal> spec = entitySpecification.specificationBuilder(List.of(criteria), Animal.class);
        assertThat(spec).isNotNull();
    }

    @Test
    void shouldBuildSpecificationForLTOperator() {
        SearchCriteria criteria = new SearchCriteria(QueryOperator.LT, "id", List.of("5"));
        Specification<Animal> spec = entitySpecification.specificationBuilder(List.of(criteria), Animal.class);
        assertThat(spec).isNotNull();
    }

    @Test
    void shouldBuildSpecificationForGTOperator() {
        SearchCriteria criteria = new SearchCriteria(QueryOperator.GT, "id", List.of("2"));
        Specification<Animal> spec = entitySpecification.specificationBuilder(List.of(criteria), Animal.class);
        assertThat(spec).isNotNull();
    }

    @Test
    void shouldBuildSpecificationForGTEOperator() {
        SearchCriteria criteria = new SearchCriteria(QueryOperator.GTE, "id", List.of("3"));
        Specification<Animal> spec = entitySpecification.specificationBuilder(List.of(criteria), Animal.class);
        assertThat(spec).isNotNull();
    }

    @Test
    void shouldBuildSpecificationForLTEOperator() {
        SearchCriteria criteria = new SearchCriteria(QueryOperator.LTE, "id", List.of("7"));
        Specification<Animal> spec = entitySpecification.specificationBuilder(List.of(criteria), Animal.class);
        assertThat(spec).isNotNull();
    }

    @Test
    void shouldBuildSpecificationForBetweenOperator() {
        SearchCriteria criteria = new SearchCriteria(QueryOperator.BETWEEN, "id", List.of("1", "5"));
        Specification<Animal> spec = entitySpecification.specificationBuilder(List.of(criteria), Animal.class);
        assertThat(spec).isNotNull();
    }

    @Test
    void shouldBuildSpecificationForINOperator() {
        SearchCriteria criteria = new SearchCriteria(QueryOperator.IN, "type", List.of("Mammal", "Bird"));
        Specification<Animal> spec = entitySpecification.specificationBuilder(List.of(criteria), Animal.class);
        assertThat(spec).isNotNull();
    }

    @Test
    void shouldBuildSpecificationForNOTINOperator() {
        SearchCriteria criteria = new SearchCriteria(QueryOperator.NOT_IN, "type", List.of("Fish", "Reptile"));
        Specification<Animal> spec = entitySpecification.specificationBuilder(List.of(criteria), Animal.class);
        assertThat(spec).isNotNull();
    }

    @Test
    void shouldBuildSpecificationForLIKEOperator() {
        SearchCriteria criteria = new SearchCriteria(QueryOperator.LIKE, "name", List.of("%Lion%"));
        Specification<Animal> spec = entitySpecification.specificationBuilder(List.of(criteria), Animal.class);
        assertThat(spec).isNotNull();
    }

    @Test
    void shouldBuildSpecificationForCONTAINSOperator() {
        SearchCriteria criteria = new SearchCriteria(QueryOperator.CONTAINS, "name", List.of("ar"));
        Specification<Animal> spec = entitySpecification.specificationBuilder(List.of(criteria), Animal.class);
        assertThat(spec).isNotNull();
    }

    @Test
    void shouldBuildSpecificationForSTARTS_WITHOperator() {
        SearchCriteria criteria = new SearchCriteria(QueryOperator.STARTS_WITH, "name", List.of("E"));
        Specification<Animal> spec = entitySpecification.specificationBuilder(List.of(criteria), Animal.class);
        assertThat(spec).isNotNull();
    }

    @Test
    void shouldBuildSpecificationForENDS_WITHOperator() {
        SearchCriteria criteria = new SearchCriteria(QueryOperator.ENDS_WITH, "name", List.of("e"));
        Specification<Animal> spec = entitySpecification.specificationBuilder(List.of(criteria), Animal.class);
        assertThat(spec).isNotNull();
    }

    @Test
    @Disabled("Not Implemented yet")
    void shouldBuildSpecificationForANDOperator() {
        SearchCriteria criteria1 = new SearchCriteria(QueryOperator.EQ, "type", List.of("Bird"));
        SearchCriteria criteria2 = new SearchCriteria(QueryOperator.EQ, "habitat", List.of("Forest"));
        SearchCriteria criteriaAnd = new SearchCriteria(QueryOperator.AND, null, null);
        Specification<Animal> spec =
                entitySpecification.specificationBuilder(List.of(criteria1, criteriaAnd, criteria2), Animal.class);
        assertThat(spec).isNotNull();
    }

    @Test
    void shouldBuildSpecificationForOROperator() {
        SearchCriteria criteriaOr = new SearchCriteria(QueryOperator.OR, "type", List.of("Amphibian", "Fish"));
        Specification<Animal> spec = entitySpecification.specificationBuilder(List.of(criteriaOr), Animal.class);
        assertThat(spec).isNotNull();
    }
}
