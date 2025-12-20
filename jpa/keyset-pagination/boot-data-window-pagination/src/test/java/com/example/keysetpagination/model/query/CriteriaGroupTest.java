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
}
