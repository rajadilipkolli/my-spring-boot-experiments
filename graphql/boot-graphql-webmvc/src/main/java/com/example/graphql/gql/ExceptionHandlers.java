package com.example.graphql.gql;

import com.example.graphql.exception.RestControllerException;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import java.util.List;
import org.springframework.graphql.execution.DataFetcherExceptionResolver;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ExceptionHandlers implements DataFetcherExceptionResolver {
    @Override
    public Mono<List<GraphQLError>> resolveException(
            Throwable exception, DataFetchingEnvironment environment) {
        if (exception instanceof RestControllerException restControllerException) {
            return Mono.fromCallable(
                    () ->
                            List.of(
                                    GraphqlErrorBuilder.newError(environment)
                                            .errorType(ErrorType.NOT_FOUND)
                                            .message(restControllerException.getMessage())
                                            .build()));
        }
        return Mono.fromCallable(
                () ->
                        List.of(
                                GraphqlErrorBuilder.newError(environment)
                                        .errorType(ErrorType.INTERNAL_ERROR)
                                        .message(exception.getMessage())
                                        .build()));
    }
}
