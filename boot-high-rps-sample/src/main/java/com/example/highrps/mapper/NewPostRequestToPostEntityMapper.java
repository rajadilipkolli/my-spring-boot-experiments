package com.example.highrps.mapper;

import com.example.highrps.entities.PostEntity;
import com.example.highrps.entities.PostTagEntity;
import com.example.highrps.entities.TagEntity;
import com.example.highrps.model.request.NewPostRequest;
import com.example.highrps.model.request.TagRequest;
import com.example.highrps.repository.TagRepository;
import java.util.List;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.springframework.util.CollectionUtils;

// After Mapping will not be set if we use builder pattern, hence disabled it
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        uses = {TagRepository.class})
public interface NewPostRequestToPostEntityMapper {

    @Mapping(target = "tags", ignore = true)
    @Mapping(
            target = "publishedAt",
            expression = "java(newPostRequest.published() ? java.time.LocalDateTime.now() : null)")
    PostEntity convert(NewPostRequest newPostRequest, @Context TagRepository tagRepository);

    @Mapping(target = "tags", ignore = true)
    void updatePostEntity(
            NewPostRequest newPostRequest, @MappingTarget PostEntity postEntity, @Context TagRepository tagRepository);

    @AfterMapping
    default void afterMapping(
            NewPostRequest newPostRequest, @MappingTarget PostEntity postEntity, @Context TagRepository tagRepository) {
        if (!CollectionUtils.isEmpty(newPostRequest.tags())) {

            List<TagEntity> tagEntitiesFromDb = postEntity.getTags().stream()
                    .map(PostTagEntity::getTagEntity)
                    .toList();

            // Tag Entities To remove
            tagEntitiesFromDb.stream()
                    .filter(tagEntity -> !newPostRequest.tags().stream()
                            .map(TagRequest::tagName)
                            .toList()
                            .contains(tagEntity.getTagName()))
                    .forEach(postEntity::removeTag);

            List<TagEntity> tagEntitiesToUpdate = tagEntitiesFromDb.stream()
                    .filter(tagEntity -> newPostRequest.tags().stream()
                            .map(TagRequest::tagName)
                            .toList()
                            .contains(tagEntity.getTagName()))
                    .toList();

            for (TagEntity tagEntity : tagEntitiesToUpdate) {
                for (TagRequest tagRequest : newPostRequest.tags()) {
                    if (tagEntity.getTagName().equalsIgnoreCase(tagRequest.tagName())) {
                        tagEntity.setTagDescription(tagRequest.tagDescription());
                        break;
                    }
                }
            }

            // new TagEntites to Insert
            newPostRequest.tags().stream()
                    .filter(tagsRequest -> !tagEntitiesToUpdate.stream()
                            .map(TagEntity::getTagName)
                            .toList()
                            .contains(tagsRequest.tagName()))
                    .forEach(tagsRequest -> postEntity.addTag(getTagEntity(tagRepository, tagsRequest)));
        }
    }

    default TagEntity getTagEntity(TagRepository tagRepository, TagRequest tagsRequest) {
        return tagRepository
                .findByTagNameIgnoreCase(tagsRequest.tagName())
                .orElseGet(() -> tagRepository.save(new TagEntity()
                        .setTagName(tagsRequest.tagName())
                        .setTagDescription(tagsRequest.tagDescription())));
    }
}
