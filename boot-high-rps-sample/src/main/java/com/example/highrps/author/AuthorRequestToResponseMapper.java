package com.example.highrps.author;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface AuthorRequestToResponseMapper {

    AuthorResponse mapToAuthorResponse(AuthorRequest newAuthorRequest);
}
