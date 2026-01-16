package com.example.highrps.mapper;

import com.example.highrps.model.request.NewPostRequest;
import com.example.highrps.model.response.PostResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface PostRequestToResponseMapper {

    PostResponse toResponse(NewPostRequest newPostRequest);
}
