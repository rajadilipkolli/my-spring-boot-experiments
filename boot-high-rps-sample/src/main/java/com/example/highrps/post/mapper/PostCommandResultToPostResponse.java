package com.example.highrps.post.mapper;

import com.example.highrps.post.command.PostCommandResult;
import com.example.highrps.post.domain.PostResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface PostCommandResultToPostResponse {

    PostResponse convert(PostCommandResult postCommandResult);
}
