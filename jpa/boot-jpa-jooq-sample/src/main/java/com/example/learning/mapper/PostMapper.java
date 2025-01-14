package com.example.learning.mapper;

import com.example.learning.entities.Post;
import com.example.learning.entities.PostComment;
import com.example.learning.entities.Tag;
import com.example.learning.model.request.PostCommentRequest;
import com.example.learning.model.request.PostRequest;
import com.example.learning.model.request.TagRequest;
import org.mapstruct.*;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
@DecoratedWith(PostMapperDecorator.class)
public interface PostMapper {

    Post postRequestToEntity(PostRequest postRequest, String userName);

    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(source = "userName", target = "details.createdBy")
    Post postDtoToPostIgnoringChild(PostRequest postRequest, String userName);

    @Mapping(target = "post", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(source = "review", target = "content")
    PostComment postCommentRequestToPostComment(PostCommentRequest postCommentRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "name", target = "tagName")
    @Mapping(source = "description", target = "tagDescription")
    Tag tagRequestToTag(TagRequest tagRequest);
}
