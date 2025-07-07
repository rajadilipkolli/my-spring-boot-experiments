package com.example.keysetpagination.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.keysetpagination.common.ContainersConfig;
import com.example.keysetpagination.config.JpaAuditConfig;
import com.example.keysetpagination.entities.Animal;
import com.example.keysetpagination.model.query.CriteriaGroup;
import com.example.keysetpagination.model.query.QueryOperator;
import com.example.keysetpagination.model.query.SearchCriteria;
import com.example.keysetpagination.repositories.AnimalRepository;
import com.example.keysetpagination.services.EntitySpecification;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.domain.Specification;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import({ContainersConfig.class, JpaAuditConfig.class})
class EntitySpecificationTest {

    private EntitySpecification<Animal> entitySpecification;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private AnimalRepository animalRepository;

    @BeforeAll
    void setUp() {
        entitySpecification = new EntitySpecification<>(entityManager);
        // Add test data
        Animal mammal = new Animal().setName("Lion").setType("Mammal").setHabitat("Savanna");
        Animal bird = new Animal().setName("Eagle").setType("Bird").setHabitat("Forest");
        Animal fish = new Animal().setName("Shark").setType("Fish").setHabitat("Ocean");
        animalRepository.saveAll(List.of(mammal, bird, fish));
    }

    @Test
    void shouldBuildSpecificationForEQOperator() {
        SearchCriteria criteria = new SearchCriteria(QueryOperator.EQ, "type", List.of("Mammal"));
        Specification<Animal> spec = entitySpecification.specificationBuilder(List.of(criteria), Animal.class);
        assertThat(spec).isNotNull();
        List<Animal> results = animalRepository.findAll(spec);
        assertThat(results).isNotEmpty().extracting("type").containsOnly("Mammal");
    }

    @Test
    void shouldBuildSpecificationForNEOperator() {
        SearchCriteria criteria = new SearchCriteria(QueryOperator.NE, "type", List.of("Reptile"));
        Specification<Animal> spec = entitySpecification.specificationBuilder(List.of(criteria), Animal.class);
        assertThat(spec).isNotNull();
        List<Animal> results = animalRepository.findAll(spec);
        assertThat(results).isNotEmpty().extracting("type").containsOnly("Mammal", "Bird", "Fish");
    }

    @Test
    void shouldBuildSpecificationForLTOperator() {
        SearchCriteria criteria = new SearchCriteria(QueryOperator.LT, "id", List.of("5"));
        Specification<Animal> spec = entitySpecification.specificationBuilder(List.of(criteria), Animal.class);
        assertThat(spec).isNotNull();
        List<Animal> results = animalRepository.findAll(spec);
        assertThat(results).isNotEmpty().extracting("type").containsOnly("Mammal", "Bird", "Fish");
    }

    @Test
    void shouldBuildSpecificationForGTOperator() {
        SearchCriteria criteria = new SearchCriteria(QueryOperator.GT, "id", List.of("2"));
        Specification<Animal> spec = entitySpecification.specificationBuilder(List.of(criteria), Animal.class);
        assertThat(spec).isNotNull();
        List<Animal> results = animalRepository.findAll(spec);
        assertThat(results).isNotEmpty().extracting("type").containsOnly("Fish");
    }

    @Test
    void shouldBuildSpecificationForGTEOperator() {
        SearchCriteria criteria = new SearchCriteria(QueryOperator.GTE, "id", List.of("3"));
        Specification<Animal> spec = entitySpecification.specificationBuilder(List.of(criteria), Animal.class);
        assertThat(spec).isNotNull();
        List<Animal> results = animalRepository.findAll(spec);
        assertThat(results).isNotEmpty().extracting("type").containsOnly("Fish");
    }

    @Test
    void shouldBuildSpecificationForLTEOperator() {
        SearchCriteria criteria = new SearchCriteria(QueryOperator.LTE, "id", List.of("7"));
        Specification<Animal> spec = entitySpecification.specificationBuilder(List.of(criteria), Animal.class);
        assertThat(spec).isNotNull();
        List<Animal> results = animalRepository.findAll(spec);
        assertThat(results).isNotEmpty().extracting("type").containsOnly("Mammal", "Bird", "Fish");
    }

    @Test
    void shouldBuildSpecificationForBetweenOperator() {
        SearchCriteria criteria = new SearchCriteria(QueryOperator.BETWEEN, "id", List.of("1", "5"));
        Specification<Animal> spec = entitySpecification.specificationBuilder(List.of(criteria), Animal.class);
        assertThat(spec).isNotNull();
        List<Animal> results = animalRepository.findAll(spec);
        assertThat(results).isNotEmpty().extracting("type").containsOnly("Mammal", "Bird", "Fish");
    }

    @Test
    void shouldBuildSpecificationForINOperator() {
        SearchCriteria criteria = new SearchCriteria(QueryOperator.IN, "type", List.of("Mammal", "Bird"));
        Specification<Animal> spec = entitySpecification.specificationBuilder(List.of(criteria), Animal.class);
        assertThat(spec).isNotNull();
        List<Animal> results = animalRepository.findAll(spec);
        assertThat(results).isNotEmpty().extracting("type").containsOnly("Mammal", "Bird");
    }

    @Test
    void shouldBuildSpecificationForNOTINOperator() {
        SearchCriteria criteria = new SearchCriteria(QueryOperator.NOT_IN, "type", List.of("Fish", "Reptile"));
        Specification<Animal> spec = entitySpecification.specificationBuilder(List.of(criteria), Animal.class);
        assertThat(spec).isNotNull();
        List<Animal> results = animalRepository.findAll(spec);
        assertThat(results).isNotEmpty().extracting("type").containsOnly("Mammal", "Bird");
    }

    @Test
    void shouldBuildSpecificationForLIKEOperator() {
        SearchCriteria criteria = new SearchCriteria(QueryOperator.LIKE, "name", List.of("%Lion%"));
        Specification<Animal> spec = entitySpecification.specificationBuilder(List.of(criteria), Animal.class);
        assertThat(spec).isNotNull();
        List<Animal> results = animalRepository.findAll(spec);
        assertThat(results).isNotEmpty().extracting("type").containsOnly("Mammal");
    }

    @Test
    void shouldBuildSpecificationForCONTAINSOperator() {
        SearchCriteria criteria = new SearchCriteria(QueryOperator.CONTAINS, "name", List.of("ar"));
        Specification<Animal> spec = entitySpecification.specificationBuilder(List.of(criteria), Animal.class);
        assertThat(spec).isNotNull();
        List<Animal> results = animalRepository.findAll(spec);
        assertThat(results).isNotEmpty().extracting("type").containsOnly("Fish");
    }

    @Test
    void shouldBuildSpecificationForSTARTS_WITHOperator() {
        SearchCriteria criteria = new SearchCriteria(QueryOperator.STARTS_WITH, "name", List.of("E"));
        Specification<Animal> spec = entitySpecification.specificationBuilder(List.of(criteria), Animal.class);
        assertThat(spec).isNotNull();
        List<Animal> results = animalRepository.findAll(spec);
        assertThat(results).isNotEmpty().extracting("type").containsOnly("Bird");
    }

    @Test
    void shouldBuildSpecificationForENDS_WITHOperator() {
        SearchCriteria criteria = new SearchCriteria(QueryOperator.ENDS_WITH, "name", List.of("e"));
        Specification<Animal> spec = entitySpecification.specificationBuilder(List.of(criteria), Animal.class);
        assertThat(spec).isNotNull();
        List<Animal> results = animalRepository.findAll(spec);
        assertThat(results).isNotEmpty().extracting("type").containsOnly("Bird");
    }

    @Test
    void shouldBuildSpecificationForANDOperator() {
        SearchCriteria criteria1 = new SearchCriteria(QueryOperator.EQ, "type", List.of("Bird"));
        SearchCriteria criteria2 = new SearchCriteria(QueryOperator.EQ, "habitat", List.of("Forest"));
        CriteriaGroup criteriaGroup = new CriteriaGroup(QueryOperator.AND, List.of(criteria1, criteria2));
        Specification<Animal> spec = entitySpecification.specificationBuilder(List.of(criteriaGroup), Animal.class);
        assertThat(spec).isNotNull();
        List<Animal> results = animalRepository.findAll(spec);
        assertThat(results).isNotEmpty().hasSize(1);
        Animal animal = results.getFirst();
        assertThat(animal.getType()).isEqualTo("Bird");
        assertThat(animal.getHabitat()).isEqualTo("Forest");
    }

    @Test
    void shouldBuildSpecificationForOROperator() {
        SearchCriteria criteriaOr = new SearchCriteria(QueryOperator.OR, "type", List.of("Amphibian", "Fish"));
        Specification<Animal> spec = entitySpecification.specificationBuilder(List.of(criteriaOr), Animal.class);
        assertThat(spec).isNotNull();
        List<Animal> results = animalRepository.findAll(spec);
        assertThat(results).isNotEmpty().extracting("type").containsOnly("Fish");
    }

    @Test
    void shouldThrowExceptionForInvalidFieldName() {
        SearchCriteria criteria = new SearchCriteria(QueryOperator.EQ, "invalidField", List.of("value"));
        assertThatThrownBy(() -> entitySpecification.specificationBuilder(List.of(criteria), Animal.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(
                        "Unable to locate Attribute with the given name [invalidField] on this ManagedType [com.example.keysetpagination.entities.Animal]");
    }
}
