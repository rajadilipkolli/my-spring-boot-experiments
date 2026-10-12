package com.example.keysetpagination.utils;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.keysetpagination.model.query.CriteriaGroup;
import com.example.keysetpagination.model.query.ISearchCriteria;
import com.example.keysetpagination.model.query.QueryOperator;
import com.example.keysetpagination.model.query.SearchCriteria;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class SpecificationDescriberTest {

    @Test
    void describeSingleSearchCriteriaLeaf() {
        SearchCriteria<String> criteria = new SearchCriteria<>(QueryOperator.EQ, "name", List.of("Lion"));
        String result = SpecificationDescriber.describe(criteria);

        assertThat(result).isEqualTo("name EQ [\"Lion\"]");
    }

    @Test
    void describeFlatListOfMultipleCriteria() {
        List<ISearchCriteria<?>> criteriaList = List.of(
                new SearchCriteria<>(QueryOperator.EQ, "name", List.of("Lion")),
                new SearchCriteria<>(QueryOperator.GT, "age", List.of("5")));
        String result = SpecificationDescriber.describe(criteriaList);

        assertThat(result).isEqualTo("""
                name EQ ["Lion"]
                age GT ["5"]""");
    }

    @Test
    void describeCriteriaGroupWithAndOperator() {
        CriteriaGroup group = new CriteriaGroup(
                QueryOperator.AND,
                List.of(
                        new SearchCriteria<>(QueryOperator.EQ, "name", List.of("Lion")),
                        new SearchCriteria<>(QueryOperator.GT, "age", List.of("5"))));
        String result = SpecificationDescriber.describe(group);

        assertThat(result).isEqualTo("""
                AND
                  name EQ ["Lion"]
                  age GT ["5"]""");
    }

    @Test
    void describeCriteriaGroupWithOrOperator() {
        CriteriaGroup group = new CriteriaGroup(
                QueryOperator.OR,
                List.of(
                        new SearchCriteria<>(QueryOperator.EQ, "status", List.of("ACTIVE")),
                        new SearchCriteria<>(QueryOperator.EQ, "status", List.of("PENDING"))));
        String result = SpecificationDescriber.describe(group);

        assertThat(result).isEqualTo("""
                OR
                  status EQ ["ACTIVE"]
                  status EQ ["PENDING"]""");
    }

    @Test
    void describeDeeplyNestedStructure() {
        CriteriaGroup orSubgroup = new CriteriaGroup(
                QueryOperator.OR,
                List.of(
                        new SearchCriteria<>(QueryOperator.EQ, "status", List.of("ACTIVE")),
                        new SearchCriteria<>(QueryOperator.EQ, "status", List.of("PENDING"))));
        CriteriaGroup andGroup = new CriteriaGroup(
                QueryOperator.AND,
                List.of(new SearchCriteria<>(QueryOperator.EQ, "name", List.of("Lion")), orSubgroup));
        String result = SpecificationDescriber.describe(andGroup);

        assertThat(result).isEqualTo("""
                AND
                  name EQ ["Lion"]
                  OR
                    status EQ ["ACTIVE"]
                    status EQ ["PENDING"]""");

        String[] lines = result.split("\n");
        assertThat(lines[0]).isEqualTo("AND");
        assertThat(lines[1]).startsWith("  ").doesNotStartWith("   ");
        assertThat(lines[2]).startsWith("  ").doesNotStartWith("   ");
        assertThat(lines[3]).startsWith("    ").doesNotStartWith("     ");
        assertThat(lines[4]).startsWith("    ").doesNotStartWith("     ");
    }

    @Test
    void describeEdgeCasesNullInput() {
        assertThat(SpecificationDescriber.describe((ISearchCriteria<?>) null)).isEmpty();
        assertThat(SpecificationDescriber.describe((List<ISearchCriteria<?>>) null))
                .isEmpty();
    }

    @Test
    void describeEdgeCasesEmptyList() {
        assertThat(SpecificationDescriber.describe(Collections.emptyList())).isEmpty();
    }

    @Test
    void describeEdgeCasesSingleElementGroup() {
        CriteriaGroup group = new CriteriaGroup(
                QueryOperator.AND, List.of(new SearchCriteria<>(QueryOperator.EQ, "name", List.of("Lion"))));
        String result = SpecificationDescriber.describe(group);

        assertThat(result).isEqualTo("""
                AND
                  name EQ ["Lion"]""");
    }
}
