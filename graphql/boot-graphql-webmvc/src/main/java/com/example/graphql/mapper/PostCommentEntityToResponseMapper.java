package com.example.graphql.mapper;

import com.example.graphql.entities.PostCommentEntity;
import com.example.graphql.model.request.PostCommentRequest;
import com.example.graphql.model.response.PostCommentResponse;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.core.convert.converter.Converter;

@Mapper(config = MapperSpringConfig.class)
public interface PostCommentEntityToResponseMapper
        extends Converter<@NonNull PostCommentEntity, @NonNull PostCommentResponse> {

    @Mapping(source = "id", target = "commentId")
    @Mapping(source = "postEntity.id", target = "postId")
    PostCommentResponse convert(PostCommentEntity postCommentEntity);

    void updatePostCommentEntity(PostCommentRequest authorRequest, @MappingTarget PostCommentEntity postCommentEntity);

    default LocalDateTime mapToLocalDateTime(OffsetDateTime offsetDateTime) {
        if (offsetDateTime != null) {
            return offsetDateTime.toLocalDateTime();
        } else {
            return null;
        }
    }
}
