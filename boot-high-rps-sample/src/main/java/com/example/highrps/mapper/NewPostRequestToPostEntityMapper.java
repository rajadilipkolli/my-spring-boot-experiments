package com.example.highrps.mapper;

import com.example.highrps.entities.PostEntity;
import com.example.highrps.entities.PostTagEntity;
import com.example.highrps.entities.TagEntity;
import com.example.highrps.model.request.NewPostRequest;
import com.example.highrps.model.request.TagRequest;
import com.example.highrps.repository.TagRepository;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;

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
        if (newPostRequest.tags() == null) {
            return;
        }
        var requestedByName = newPostRequest.tags().stream()
                .collect(java.util.stream.Collectors.toMap(
                        t -> t.tagName().toLowerCase(java.util.Locale.ROOT),
                        java.util.function.Function.identity(),
                        (a, b) -> a));

        var existingByName = postEntity.getTags().stream()
                .map(PostTagEntity::getTagEntity)
                .collect(java.util.stream.Collectors.toMap(
                        t -> t.getTagName().toLowerCase(java.util.Locale.ROOT),
                        java.util.function.Function.identity(),
                        (a, b) -> a));

        existingByName.keySet().stream()
                .filter(name -> !requestedByName.containsKey(name))
                .forEach(name -> postEntity.removeTag(existingByName.get(name)));

        existingByName.forEach((name, tagEntity) -> {
            TagRequest req = requestedByName.get(name);
            if (req != null && !java.util.Objects.equals(tagEntity.getTagDescription(), req.tagDescription())) {
                tagEntity.setTagDescription(req.tagDescription());
            }
        });

        requestedByName.keySet().stream()
                .filter(name -> !existingByName.containsKey(name))
                .forEach(name -> postEntity.addTag(getTagEntity(tagRepository, requestedByName.get(name))));
    }

    default TagEntity getTagEntity(TagRepository tagRepository, TagRequest tagsRequest) {
        return tagRepository
                .findByTagNameIgnoreCase(tagsRequest.tagName())
                .orElseGet(() -> tagRepository.save(new TagEntity()
                        .setTagName(tagsRequest.tagName())
                        .setTagDescription(tagsRequest.tagDescription())));
    }
}
