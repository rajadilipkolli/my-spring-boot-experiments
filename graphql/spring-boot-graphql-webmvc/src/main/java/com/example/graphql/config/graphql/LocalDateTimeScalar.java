package com.example.graphql.config.graphql;

import static graphql.scalars.util.Kit.typeName;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;
import static java.time.temporal.ChronoField.OFFSET_SECONDS;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;

import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.function.Function;

// Copied from DateTimeScalar which supports OffsetDateTime
public class LocalDateTimeScalar {

    public static final GraphQLScalarType INSTANCE;

    private LocalDateTimeScalar() {}

    private static final DateTimeFormatter customOutputFormatter = getCustomDateTimeFormatter();

    static {
        Coercing<LocalDateTime, String> coercing =
                new Coercing<>() {
                    @Override
                    public String serialize(Object input) throws CoercingSerializeException {
                        LocalDateTime localDateTime;
                        if (input instanceof LocalDateTime) {
                            localDateTime = (LocalDateTime) input;
                        } else if (input instanceof ZonedDateTime) {
                            localDateTime = ((ZonedDateTime) input).toLocalDateTime();
                        } else if (input instanceof String) {
                            localDateTime =
                                    parseOffsetDateTime(
                                            input.toString(), CoercingSerializeException::new);
                        } else {
                            throw new CoercingSerializeException(
                                    "Expected something we can convert to 'java.time.OffsetDateTime' but was '"
                                            + typeName(input)
                                            + "'.");
                        }
                        try {
                            return customOutputFormatter.format(localDateTime);
                        } catch (DateTimeException e) {
                            throw new CoercingSerializeException(
                                    "Unable to turn TemporalAccessor into OffsetDateTime because of : '"
                                            + e.getMessage()
                                            + "'.");
                        }
                    }

                    @Override
                    public LocalDateTime parseValue(Object input)
                            throws CoercingParseValueException {
                        LocalDateTime localDateTime;
                        if (input instanceof LocalDateTime) {
                            localDateTime = (LocalDateTime) input;
                        } else if (input instanceof ZonedDateTime) {
                            localDateTime = ((ZonedDateTime) input).toLocalDateTime();
                        } else if (input instanceof String) {
                            localDateTime =
                                    parseOffsetDateTime(
                                            input.toString(), CoercingParseValueException::new);
                        } else {
                            throw new CoercingParseValueException(
                                    "Expected a 'String' but was '" + typeName(input) + "'.");
                        }
                        return localDateTime;
                    }

                    @Override
                    public LocalDateTime parseLiteral(Object input)
                            throws CoercingParseLiteralException {
                        if (!(input instanceof StringValue)) {
                            throw new CoercingParseLiteralException(
                                    "Expected AST type 'StringValue' but was '"
                                            + typeName(input)
                                            + "'.");
                        }
                        return parseOffsetDateTime(
                                ((StringValue) input).getValue(),
                                CoercingParseLiteralException::new);
                    }

                    @Override
                    public Value<?> valueToLiteral(Object input) {
                        String s = serialize(input);
                        return StringValue.newStringValue(s).build();
                    }

                    private LocalDateTime parseOffsetDateTime(
                            String s, Function<String, RuntimeException> exceptionMaker) {
                        try {
                            LocalDateTime parse =
                                    LocalDateTime.parse(s, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                            if (parse.get(OFFSET_SECONDS) == 0 && s.endsWith("-00:00")) {
                                throw exceptionMaker.apply(
                                        "Invalid value : '"
                                                + s
                                                + "'. Negative zero offset is not allowed");
                            }
                            return parse;
                        } catch (DateTimeParseException e) {
                            throw exceptionMaker.apply(
                                    "Invalid RFC3339 value : '"
                                            + s
                                            + "'. because of : '"
                                            + e.getMessage()
                                            + "'");
                        }
                    }
                };

        INSTANCE =
                GraphQLScalarType.newScalar()
                        .name("LocalDateTime")
                        .description(
                                "A slightly refined version of RFC-3339 compliant DateTime Scalar")
                        .specifiedByUrl(
                                "https://scalars.graphql.org/andimarek/date-time") // TODO: Change
                        // to
                        // .specifiedByURL when builder added to graphql-java
                        .coercing(coercing)
                        .build();
    }

    private static DateTimeFormatter getCustomDateTimeFormatter() {
        return new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .append(ISO_LOCAL_DATE)
                .appendLiteral('T')
                .appendValue(HOUR_OF_DAY, 2)
                .appendLiteral(':')
                .appendValue(MINUTE_OF_HOUR, 2)
                .appendLiteral(':')
                .appendValue(SECOND_OF_MINUTE, 2)
                .appendFraction(NANO_OF_SECOND, 3, 3, true)
                .appendLiteral('Z')
                .toFormatter();
    }
}
