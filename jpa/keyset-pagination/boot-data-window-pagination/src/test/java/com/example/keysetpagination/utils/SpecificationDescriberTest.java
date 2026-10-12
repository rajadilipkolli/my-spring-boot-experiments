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

    /** Verifies that a leaf description contains its field, operator, and quoted value. */
    @Test
    void describeSingleSearchCriteriaLeaf() {
        SearchCriteria<String> criteria = new SearchCriteria<>(QueryOperator.EQ, "name", List.of("Lion"));
        String result = SpecificationDescriber.describe(criteria);

        assertThat(result).isEqualTo("name EQ [\"Lion\"]");
    }

    /** Verifies that embedded double quotes and newline characters are escaped in values. */
    @Test
    void describeQuotedMultilineValue() {
        SearchCriteria<String> criteria = new SearchCriteria<>(QueryOperator.EQ, "name", List.of("The \"Lion\"\nKing"));

        assertThat(SpecificationDescriber.describe(criteria)).isEqualTo("name EQ [\"The \\\"Lion\\\"\\nKing\"]");
    }

    /** Verifies that a null value is rendered without quotes. */
    @Test
    void describeNullValue() {
        SearchCriteria<String> criteria =
                new SearchCriteria<>(QueryOperator.EQ, "name", Collections.singletonList(null));

        assertThat(SpecificationDescriber.describe(criteria)).isEqualTo("name EQ [null]");
    }

    /** Verifies that top-level criteria appear on separate lines in list order. */
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

    /** Verifies that an AND operator precedes its indented child criteria. */
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

    /** Verifies that an OR operator precedes its indented child criteria. */
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

    /** Verifies that each nested group adds exactly two spaces of indentation. */
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

    /** Verifies that both describe overloads return an empty string for null input. */
    @Test
    void describeEdgeCasesNullInput() {
        assertThat(SpecificationDescriber.describe((ISearchCriteria<?>) null)).isEmpty();
        assertThat(SpecificationDescriber.describe((List<ISearchCriteria<?>>) null))
                .isEmpty();
    }

    /** Verifies that an empty criteria list produces an empty description. */
    @Test
    void describeEdgeCasesEmptyList() {
        assertThat(SpecificationDescriber.describe(Collections.emptyList())).isEmpty();
    }

    /** Verifies that a group with one child retains its operator and child indentation. */
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
