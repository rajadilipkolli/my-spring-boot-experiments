package com.example.graphql.mapper;

import com.example.graphql.entities.AuthorEntity;
import com.example.graphql.model.response.AuthorResponse;
import org.mapstruct.Mapper;
import org.springframework.core.convert.converter.Converter;

@Mapper(config = MapperSpringConfig.class)
public interface AuthorEntityToResponseMapper extends Converter<AuthorEntity, AuthorResponse> {}
