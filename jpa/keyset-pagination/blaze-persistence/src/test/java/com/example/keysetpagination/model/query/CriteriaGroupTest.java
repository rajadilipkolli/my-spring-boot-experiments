package com.example.keysetpagination.model.query;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class CriteriaGroupTest {

    private final JsonMapper jsonMapper = new JsonMapper();

    @Test
    void testSerializationWithDiscriminator() {
        SearchCriteria<Object> sc = new SearchCriteria<>();
        sc.setField("name");
        sc.setQueryOperator(SearchCriteria.QueryOperator.EQ);
        sc.setValue("Test");

        CriteriaGroup<Object> group = new CriteriaGroup<>(LogicalOperator.OR, List.of(sc));

        String json = jsonMapper.writeValueAsString(group);

        assertThat(json).contains("\"type\":\"group\"");
        assertThat(json).contains("\"type\":\"criteria\"");
    }

    @Test
    void testDeserialization() throws Exception {
        String json = """
            {
                "type": "group",
                "operator": "OR",
                "criteriaList": [
                    {
                        "type": "criteria",
                        "field": "name",
                        "queryOperator": "EQ",
                        "values": ["Test"]
                    }
                ]
            }
            """;

        ISearchCriteria<?> criteria = jsonMapper.readValue(json, ISearchCriteria.class);

        assertThat(criteria).isInstanceOf(CriteriaGroup.class);
        CriteriaGroup<?> group = (CriteriaGroup<?>) criteria;
        assertThat(group.getOperator()).isEqualTo(LogicalOperator.OR);
        assertThat(group.getCriteriaList()).hasSize(1);
        assertThat(group.getCriteriaList().getFirst()).isInstanceOf(SearchCriteria.class);
    }

    @Test
    void testDeeplyNestedSerializationDeserialization() throws Exception {
        String json = """
            {
                "type": "group",
                "operator": "AND",
                "criteriaList": [
                    {
                        "type": "group",
                        "operator": "OR",
                        "criteriaList": [
                            {
                                "type": "criteria",
                                "field": "age",
                                "queryOperator": "GT",
                                "values": ["18"]
                            }
                        ]
                    }
                ]
            }
            """;

        ISearchCriteria<?> criteria = jsonMapper.readValue(json, ISearchCriteria.class);

        assertThat(criteria).isInstanceOf(CriteriaGroup.class);
        CriteriaGroup<?> group1 = (CriteriaGroup<?>) criteria;
        assertThat(group1.getOperator()).isEqualTo(LogicalOperator.AND);

        ISearchCriteria<?> child = group1.getCriteriaList().getFirst();
        assertThat(child).isInstanceOf(CriteriaGroup.class);

        CriteriaGroup<?> group2 = (CriteriaGroup<?>) child;
        assertThat(group2.getOperator()).isEqualTo(LogicalOperator.OR);
        assertThat(group2.getCriteriaList().getFirst()).isInstanceOf(SearchCriteria.class);
    }
}
