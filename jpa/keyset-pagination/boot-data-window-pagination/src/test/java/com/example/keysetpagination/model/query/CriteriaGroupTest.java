package com.example.keysetpagination.model.query;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

@JsonTest
class CriteriaGroupTest {

    @Autowired
    private JacksonTester<CriteriaGroup> json;

    @Test
    void serialize() throws Exception {

        CriteriaGroup criteriaGroup = new CriteriaGroup(
                QueryOperator.AND, List.of(new SearchCriteria(QueryOperator.EQ, "name", List.of("Bird"))));

        JsonContent<CriteriaGroup> result = this.json.write(criteriaGroup);

        assertThat(result).hasJsonPathStringValue("$.operator");
        assertThat(result.getJson()).isEqualToIgnoringWhitespace("""
                                {"type":"group","operator":"AND","criteriaList":[{"type":"criteria","queryOperator":"EQ","field":"name","values":["Bird"]}]}
                                """);
    }

    @Test
    void toStringWithAndGroup() {
        SearchCriteria<String> crit1 = new SearchCriteria<>(QueryOperator.EQ, "name", List.of("Lion"));
        SearchCriteria<String> crit2 = new SearchCriteria<>(QueryOperator.GT, "age", List.of("5"));
        CriteriaGroup criteriaGroup = new CriteriaGroup(QueryOperator.AND, List.of(crit1, crit2));

        assertThat(criteriaGroup.toString())
                .isEqualTo(
                        "CriteriaGroup[operator=AND, criteriaList=[SearchCriteria[queryOperator=EQ, field='name', values=[Lion]], SearchCriteria[queryOperator=GT, field='age', values=[5]]]]");
    }

    @Test
    void toStringWithOrGroup() {
        SearchCriteria<String> crit1 = new SearchCriteria<>(QueryOperator.EQ, "status", List.of("ACTIVE"));
        SearchCriteria<String> crit2 = new SearchCriteria<>(QueryOperator.EQ, "status", List.of("PENDING"));
        CriteriaGroup criteriaGroup = new CriteriaGroup(QueryOperator.OR, List.of(crit1, crit2));

        assertThat(criteriaGroup.toString())
                .isEqualTo(
                        "CriteriaGroup[operator=OR, criteriaList=[SearchCriteria[queryOperator=EQ, field='status', values=[ACTIVE]], SearchCriteria[queryOperator=EQ, field='status', values=[PENDING]]]]");
    }

    @Test
    void toStringWithNestedGroups() {
        SearchCriteria<String> crit1 = new SearchCriteria<>(QueryOperator.EQ, "name", List.of("Lion"));
        SearchCriteria<String> subCrit1 = new SearchCriteria<>(QueryOperator.EQ, "status", List.of("ACTIVE"));
        SearchCriteria<String> subCrit2 = new SearchCriteria<>(QueryOperator.EQ, "status", List.of("PENDING"));
        CriteriaGroup subGroup = new CriteriaGroup(QueryOperator.OR, List.of(subCrit1, subCrit2));
        CriteriaGroup rootGroup = new CriteriaGroup(QueryOperator.AND, List.of(crit1, subGroup));

        assertThat(rootGroup.toString())
                .isEqualTo(
                        "CriteriaGroup[operator=AND, criteriaList=[SearchCriteria[queryOperator=EQ, field='name', values=[Lion]], CriteriaGroup[operator=OR, criteriaList=[SearchCriteria[queryOperator=EQ, field='status', values=[ACTIVE]], SearchCriteria[queryOperator=EQ, field='status', values=[PENDING]]]]]]");
    }
}
