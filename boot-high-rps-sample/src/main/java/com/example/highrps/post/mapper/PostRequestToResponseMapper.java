package com.example.highrps.post.mapper;

import com.example.highrps.post.domain.PostResponse;
import com.example.highrps.post.domain.requests.NewPostRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface PostRequestToResponseMapper {

    @Mapping(source = "email", target = "authorEmail")
    PostResponse mapToPostResponse(NewPostRequest newPostRequest);
}
