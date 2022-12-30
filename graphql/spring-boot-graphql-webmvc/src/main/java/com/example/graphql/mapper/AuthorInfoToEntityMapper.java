package com.example.graphql.mapper;

import com.example.graphql.dtos.AuthorInfo;
import com.example.graphql.entities.AuthorEntity;
import org.mapstruct.Mapper;
import org.springframework.core.convert.converter.Converter;

@Mapper(config = MapperSpringConfig.class)
public interface AuthorInfoToEntityMapper extends Converter<AuthorInfo, AuthorEntity> {

    AuthorEntity convert(AuthorInfo authorInfo);
}
