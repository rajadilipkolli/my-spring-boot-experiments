package com.example.keysetpagination.model.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.keysetpagination.entities.Actor;
import com.example.keysetpagination.model.query.SearchCriteria.QueryOperator;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

class SearchCriteriaTest {

    /** Verifies invalid IN and BETWEEN value counts fail during specification creation. */
    @ParameterizedTest
    @MethodSource("invalidValues")
    void shouldRejectInvalidValuesBeforeBuildingSpecification(QueryOperator operator, List<String> values) {
        SearchCriteria<Actor> criteria = criteria(operator, values);

        assertThatThrownBy(() -> criteria.toSpecification(Actor.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        operator == QueryOperator.IN
                                ? "IN operator requires at least one value"
                                : "BETWEEN operator requires exactly 2 values");
    }

    /** Supplies missing, empty, and incorrectly sized value lists for IN and BETWEEN. */
    static Stream<Arguments> invalidValues() {
        return Stream.of(
                Arguments.of(QueryOperator.IN, null),
                Arguments.of(QueryOperator.IN, List.of()),
                Arguments.of(QueryOperator.BETWEEN, null),
                Arguments.of(QueryOperator.BETWEEN, List.of()),
                Arguments.of(QueryOperator.BETWEEN, List.of("1")),
                Arguments.of(QueryOperator.BETWEEN, List.of("1", "2", "3")));
    }

    /** Verifies IN and BETWEEN values are parsed as entity IDs before predicate creation. */
    @ParameterizedTest
    @MethodSource("validValues")
    void shouldParseValidValues(QueryOperator operator, List<String> values) {
        Root<Actor> root = mock(Root.class);
        Path<Long> path = mock(Path.class);
        CriteriaBuilder builder = mock(CriteriaBuilder.class);
        when(root.<Long>get("id")).thenReturn(path);

        criteria(operator, values).toSpecification(Actor.class).toPredicate(root, null, builder);

        if (operator == QueryOperator.IN) {
            verify(path).in(List.of(1L, 2L));
        } else {
            verify(builder).between(path, 1L, 2L);
        }
    }

    /** Supplies valid pairs of string IDs for IN and BETWEEN predicates. */
    static Stream<Arguments> validValues() {
        return Stream.of(
                Arguments.of(QueryOperator.IN, List.of("1", "2")),
                Arguments.of(QueryOperator.BETWEEN, List.of("1", "2")));
    }

    /** Verifies equality filters with missing or empty values can create a specification. */
    @ParameterizedTest
    @NullAndEmptySource
    void shouldAllowNullEquality(List<String> values) {
        SearchCriteria<Actor> criteria = criteria(QueryOperator.EQ, values);
        assertThat(criteria.toSpecification(Actor.class)).isNotNull();
    }

    /** Creates an actor ID filter with the supplied operator and raw values. */
    private SearchCriteria<Actor> criteria(QueryOperator operator, List<String> values) {
        SearchCriteria<Actor> criteria = new SearchCriteria<>();
        criteria.setField("id");
        criteria.setQueryOperator(operator);
        criteria.setValues(values);
        return criteria;
    }
}
