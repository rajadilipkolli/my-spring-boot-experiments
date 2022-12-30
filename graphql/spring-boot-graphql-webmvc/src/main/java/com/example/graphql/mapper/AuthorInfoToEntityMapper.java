package com.example.graphql.mapper;

import com.example.graphql.dtos.AuthorInfo;
import com.example.graphql.entities.Author;
import org.mapstruct.Mapper;
import org.springframework.core.convert.converter.Converter;

@Mapper(config = MapperSpringConfig.class)
public interface AuthorInfoToEntityMapper extends Converter<AuthorInfo, Author> {

    Author convert(AuthorInfo authorInfo);
}
