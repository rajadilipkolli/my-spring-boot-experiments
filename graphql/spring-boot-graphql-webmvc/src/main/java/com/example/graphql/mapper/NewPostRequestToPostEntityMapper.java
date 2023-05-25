package com.example.graphql.mapper;

import com.example.graphql.entities.PostEntity;
import com.example.graphql.entities.TagEntity;
import com.example.graphql.model.request.NewPostRequest;
import com.example.graphql.model.request.TagsRequest;
import com.example.graphql.repositories.TagRepository;
import org.mapstruct.AfterMapping;
import org.mapstruct.Builder;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(
        builder = @Builder(disableBuilder = true),
        uses = {TagRepository.class})
public interface NewPostRequestToPostEntityMapper {

    @Mapping(target = "tags", ignore = true)
    @Mapping(
            target = "publishedAt",
            expression = "java(newPostRequest.published() ? java.time.LocalDateTime.now() : null)")
    PostEntity convert(NewPostRequest newPostRequest, @Context TagRepository tagRepository);

    void updatePostEntity(NewPostRequest newPostRequest, @MappingTarget PostEntity postEntity);

    @AfterMapping
    default void afterMapping(
            NewPostRequest newPostRequest,
            @MappingTarget PostEntity postEntity,
            @Context TagRepository tagRepository) {
        if (null != newPostRequest.tags()) {
            newPostRequest
                    .tags()
                    .forEach(
                            tagsRequest ->
                                    postEntity.addTag(getTagEntity(tagRepository, tagsRequest)));
        }
    }

    default TagEntity getTagEntity(TagRepository tagRepository, TagsRequest tagsRequest) {
        return tagRepository
                .findByTagNameIgnoreCase(tagsRequest.tagName())
                .orElseGet(
                        () ->
                                tagRepository.save(
                                        new TagEntity(
                                                tagsRequest.tagName(),
                                                tagsRequest.tagDescription())));
    }
}
