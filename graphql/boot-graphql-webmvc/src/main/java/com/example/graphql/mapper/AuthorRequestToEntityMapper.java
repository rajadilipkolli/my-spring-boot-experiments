package com.example.graphql.mapper;

import com.example.graphql.entities.AuthorEntity;
import com.example.graphql.model.request.AuthorRequest;
import java.time.LocalDateTime;
import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.core.convert.converter.Converter;

@Mapper(config = MapperSpringConfig.class, imports = LocalDateTime.class)
public interface AuthorRequestToEntityMapper extends Converter<@NonNull AuthorRequest, @NonNull AuthorEntity> {

    @Mapping(target = "registeredAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "postEntities", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "id", ignore = true)
    AuthorEntity convert(AuthorRequest authorRequest);

    void upDateAuthorEntity(AuthorRequest authorRequest, @MappingTarget AuthorEntity authorEntity);
}
