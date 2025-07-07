package com.example.graphql.mapper;

import com.example.graphql.entities.PostCommentEntity;
import com.example.graphql.model.request.PostCommentRequest;
import com.example.graphql.repositories.PostRepository;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

// After Mapping will not be set if we use builder pattern, hence disabled it
@Mapper
public interface PostCommentRequestToEntityMapper {

    @Mapping(
            target = "publishedAt",
            expression = "java(postCommentRequest.published() ? java.time.OffsetDateTime.now() : null)")
    PostCommentEntity covert(PostCommentRequest postCommentRequest, @Context PostRepository postRepository);

    @AfterMapping
    default void afterMapping(
            PostCommentRequest postCommentRequest,
            @MappingTarget PostCommentEntity postCommentEntity,
            @Context PostRepository postRepository) {
        // Set the PostEntity reference using the postId from the request
        postCommentEntity.setPostEntity(postRepository.getReferenceById(Long.valueOf(postCommentRequest.postId())));
    }
}
