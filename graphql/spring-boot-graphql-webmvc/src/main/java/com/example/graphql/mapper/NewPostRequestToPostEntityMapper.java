package com.example.graphql.mapper;

import com.example.graphql.entities.PostEntity;
import com.example.graphql.entities.TagEntity;
import com.example.graphql.model.request.NewPostRequest;
import com.example.graphql.model.request.TagsRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import org.mapstruct.AfterMapping;
import org.mapstruct.Builder;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.core.convert.converter.Converter;

@Mapper(
        config = MapperSpringConfig.class,
        builder = @Builder(disableBuilder = true),
        uses = {EntityManagerFactory.class})
public interface NewPostRequestToPostEntityMapper extends Converter<NewPostRequest, PostEntity> {

    @Mapping(target = "tags", ignore = true)
    @Mapping(
            target = "publishedAt",
            expression = "java(newPostRequest.published() ? java.time.LocalDateTime.now() : null)")
    PostEntity convert(NewPostRequest newPostRequest);

    @AfterMapping
    default void afterMapping(
            NewPostRequest newPostRequest,
            @MappingTarget PostEntity postEntity,
            @Context EntityManager entityManager) {
        newPostRequest
                .tags()
                .forEach(
                        tagsRequest -> postEntity.addTag(getTagEntity(entityManager, tagsRequest)));
    }

    default TagEntity getTagEntity(EntityManager entityManager, TagsRequest tagsRequest) {

        TypedQuery<TagEntity> query =
                entityManager.createQuery(
                        "SELECT b FROM TagEntity b WHERE b.tagName = :name", TagEntity.class);
        query.setParameter("name", tagsRequest.tagName());
        TagEntity result = query.getSingleResult();
        if (null == result) {
            TagEntity tag = new TagEntity(tagsRequest.tagName(), tagsRequest.tagDescription());
            entityManager.getTransaction().begin();
            entityManager.persist(tag);
            entityManager.flush();
            entityManager.refresh(tag);
            entityManager.getTransaction().commit();
            result = tag;
        }
        return result;
    }
}
