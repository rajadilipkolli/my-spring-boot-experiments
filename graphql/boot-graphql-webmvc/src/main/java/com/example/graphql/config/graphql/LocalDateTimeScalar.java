package com.example.graphql.config.graphql;

import static graphql.scalars.util.Kit.typeName;

import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

// Copied from DateTimeScalar which supports OffsetDateTime
public class LocalDateTimeScalar {

    public static final GraphQLScalarType INSTANCE;

    private LocalDateTimeScalar() {}

    static {
        Coercing<LocalDateTime, String> coercing = new Coercing<>() {
            @Override
            public String serialize(Object dataFetcherResult, GraphQLContext graphQLContext, Locale locale)
                    throws CoercingSerializeException {
                if (dataFetcherResult instanceof LocalDateTime localDateTime) {
                    try {
                        return localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    } catch (ClassCastException | DateTimeException exception) {
                        throw new CoercingSerializeException(
                                "Unable to turn TemporalAccessor into LocalDateTime because of : '"
                                        + exception.getMessage()
                                        + "'.");
                    }
                } else {
                    throw new CoercingSerializeException(
                            "Expected something we can convert to 'java.time.LocalDateTime' but was '"
                                    + typeName(dataFetcherResult)
                                    + "'.");
                }
            }

            @Override
            public LocalDateTime parseValue(Object input, GraphQLContext graphQLContext, Locale locale)
                    throws CoercingParseValueException {
                // Will be String if the value is specified via external variables object,
                // and a StringValue
                // if provided direct in the query.
                return switch (input) {
                    case StringValue stringValue -> parseString(stringValue.getValue());
                    case String inputString -> parseString(inputString);
                    case LocalDateTime localDateTime -> localDateTime;
                    default ->
                        throw new CoercingParseValueException("Expected a 'String' but was '" + typeName(input) + "'.");
                };
            }

            private LocalDateTime parseString(String input) {
                try {
                    return LocalDateTime.parse(input, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                } catch (DateTimeParseException parseException) {
                    throw new CoercingParseValueException("Expected a 'String' but was '" + typeName(input) + "'.");
                }
            }

            @Override
            public LocalDateTime parseLiteral(
                    Value<?> input, CoercedVariables variables, GraphQLContext graphQLContext, Locale locale)
                    throws CoercingParseLiteralException {
                if (!(input instanceof StringValue)) {
                    throw new CoercingParseLiteralException(
                            "Expected AST type 'StringValue' but was '" + typeName(input) + "'.");
                }
                try {
                    return parseValue(input, graphQLContext, locale);
                } catch (CoercingParseValueException exception) {
                    throw new CoercingParseLiteralException(exception);
                }
            }

            @Override
            public Value<?> valueToLiteral(Object input, GraphQLContext graphQLContext, Locale locale) {
                String s = serialize(input, graphQLContext, locale);
                return StringValue.newStringValue(s).build();
            }
        };

        INSTANCE = GraphQLScalarType.newScalar()
                .name("LocalDateTime")
                .description(
                        "A date-time without a time-zone in the ISO-8601 calendar system, formatted as '2011-12-03T10:15:30")
                .specifiedByUrl("https://scalars.graphql.org/andimarek/date-time.html")
                // TODO: Change to .specifiedByURL when builder added to graphql-java
                .coercing(coercing)
                .build();
    }
}
