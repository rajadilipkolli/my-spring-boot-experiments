package com.example.highrps.mapper;

import com.example.highrps.model.request.AuthorRequest;
import com.example.highrps.model.response.AuthorResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface AuthorRequestToResponseMapper {

    AuthorResponse mapToAuthorResponse(AuthorRequest newAuthorRequest);
}
