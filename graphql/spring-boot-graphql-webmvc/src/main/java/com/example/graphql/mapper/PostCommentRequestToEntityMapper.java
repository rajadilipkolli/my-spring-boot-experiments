package com.example.graphql.mapper;

import com.example.graphql.entities.PostCommentEntity;
import com.example.graphql.model.request.PostCommentRequest;
import com.example.graphql.repositories.PostRepository;
import org.mapstruct.*;

// After Mapping will not be set if we use builder pattern, hence disabled it
@Mapper(builder = @Builder(disableBuilder = true))
public interface PostCommentRequestToEntityMapper {

    @Mapping(
            target = "publishedAt",
            expression =
                    "java(postCommentRequest.published() ? java.time.OffsetDateTime.now() : null)")
    PostCommentEntity covert(
            PostCommentRequest postCommentRequest, @Context PostRepository postRepository);

    @AfterMapping
    default void afterMapping(
            PostCommentRequest postCommentRequest,
            @MappingTarget PostCommentEntity postCommentEntity,
            @Context PostRepository postRepository) {
        // Set the PostEntity reference using the postId from the request
        postCommentEntity.setPostEntity(
                postRepository.getReferenceById(postCommentRequest.postId()));
    }
}
