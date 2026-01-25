package com.example.highrps.mapper;

import com.example.highrps.entities.AuthorEntity;
import com.example.highrps.model.request.AuthorRequest;
import java.time.LocalDateTime;
import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.springframework.core.convert.converter.Converter;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        imports = LocalDateTime.class)
public interface AuthorRequestToEntityMapper extends Converter<@NonNull AuthorRequest, @NonNull AuthorEntity> {

    @Mapping(target = "registeredAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "postEntities", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "id", ignore = true)
    AuthorEntity convert(AuthorRequest authorRequest);

    void updateAuthorEntity(AuthorRequest authorRequest, @MappingTarget AuthorEntity authorEntity);
}
