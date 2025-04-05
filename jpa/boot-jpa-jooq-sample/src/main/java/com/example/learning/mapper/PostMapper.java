package com.example.learning.mapper;

import com.example.learning.entities.Post;
import com.example.learning.entities.PostComment;
import com.example.learning.entities.PostTag;
import com.example.learning.entities.Tag;
import com.example.learning.model.request.PostCommentRequest;
import com.example.learning.model.request.PostRequest;
import com.example.learning.model.request.TagRequest;
import com.example.learning.model.response.PostResponse;
import com.example.learning.model.response.TagResponse;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.DecoratedWith;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED)
@DecoratedWith(PostMapperDecorator.class)
public interface PostMapper {

    @Mapping(target = "createdAt", ignore = true)
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

    @IterableMapping(elementTargetType = PostComment.class)
    List<PostComment> postCommentsRequestListToPostCommentList(List<PostCommentRequest> comments);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "name", target = "tagName")
    @Mapping(source = "description", target = "tagDescription")
    Tag tagRequestToTag(TagRequest tagRequest);

    @IterableMapping(elementTargetType = Tag.class)
    List<Tag> tagRequestListToTagList(List<TagRequest> tags);

    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "details", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateReferenceValues(PostRequest postRequest, @MappingTarget Post post);

    @Mapping(target = "author", source = "details.createdBy")
    PostResponse postToPostResponse(Post updatedPost);

    @Mapping(target = "name", source = "tag.tagName")
    @Mapping(target = "description", source = "tag.tagDescription")
    TagResponse postTagToTagResponse(PostTag postTag);

    // Custom mapping method
    default OffsetDateTime map(LocalDateTime value) {
        return value != null ? value.atOffset(ZoneOffset.UTC) : null;
    }
}
