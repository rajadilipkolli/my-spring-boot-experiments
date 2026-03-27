package com.example.highrps.post.mapper;

import com.example.highrps.entities.PostEntity;
import com.example.highrps.entities.PostTagEntity;
import com.example.highrps.entities.TagEntity;
import com.example.highrps.post.domain.TagResponse;
import com.example.highrps.post.domain.requests.NewPostRequest;
import com.example.highrps.repository.jpa.TagRepository;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
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
    @Mapping(target = "postRefId", source = "postId")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "authorEntity", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "details.id", ignore = true)
    @Mapping(target = "details.postEntity", ignore = true)
    @Mapping(target = "details.createdAt", ignore = true)
    @Mapping(target = "details.modifiedAt", ignore = true)
    PostEntity convert(NewPostRequest newPostRequest, @Context TagRepository tagRepository);

    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "postRefId", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "authorEntity", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "details.id", ignore = true)
    @Mapping(target = "details.postEntity", ignore = true)
    @Mapping(target = "details.createdAt", ignore = true)
    @Mapping(target = "details.modifiedAt", ignore = true)
    void updatePostEntity(
            NewPostRequest newPostRequest, @MappingTarget PostEntity postEntity, @Context TagRepository tagRepository);

    @AfterMapping
    default void afterMapping(
            NewPostRequest newPostRequest, @MappingTarget PostEntity postEntity, @Context TagRepository tagRepository) {
        if (newPostRequest.tags() == null) {
            return;
        }
        var requestedByName = newPostRequest.tags().stream()
                .collect(Collectors.toMap(t -> t.tagName().toLowerCase(Locale.ROOT), Function.identity(), (a, b) -> a));

        var existingByName = postEntity.getTags().stream()
                .map(PostTagEntity::getTagEntity)
                .collect(Collectors.toMap(
                        t -> t.getTagName().toLowerCase(Locale.ROOT), Function.identity(), (a, b) -> a));

        existingByName.keySet().stream()
                .filter(name -> !requestedByName.containsKey(name))
                .forEach(name -> postEntity.removeTag(existingByName.get(name)));

        existingByName.forEach((name, tagEntity) -> {
            TagResponse resp = requestedByName.get(name);
            if (resp != null && !Objects.equals(tagEntity.getTagDescription(), resp.tagDescription())) {
                tagEntity.setTagDescription(resp.tagDescription());
            }
        });

        requestedByName.keySet().stream()
                .filter(name -> !existingByName.containsKey(name))
                .forEach(name -> postEntity.addTag(getTagEntity(tagRepository, requestedByName.get(name))));
    }

    default TagEntity getTagEntity(TagRepository tagRepository, TagResponse tagResponse) {
        return tagRepository.findByTagNameIgnoreCase(tagResponse.tagName()).orElseGet(() -> {
            TagEntity tagEntity =
                    new TagEntity().setTagName(tagResponse.tagName()).setTagDescription(tagResponse.tagDescription());
            tagEntity.setCreatedAt(LocalDateTime.now());
            return tagRepository.save(tagEntity);
        });
    }
}
