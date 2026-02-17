package com.example.highrps.postcomment.domain;

import com.example.highrps.entities.PostCommentEntity;
import com.example.highrps.entities.PostCommentRedis;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class PostCommentMapper {

    @Autowired
    protected ObjectMapper objectMapper;

    @Mapping(target = "postId", source = "postEntity.id")
    public abstract PostCommentResult toResult(PostCommentEntity entity);

    public abstract List<PostCommentResult> toResultList(List<PostCommentEntity> entities);

    public abstract PostCommentResult toResultFromRequest(PostCommentRequest request);

    public abstract PostCommentResult toResultFromRedis(PostCommentRedis redis);

    public abstract PostCommentRedis toRedis(PostCommentRequest request);

    /**
     * Serialize PostCommentResult to JSON string for local cache.
     */
    public String toJson(PostCommentResult result) {
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to serialize PostCommentResult to JSON", e);
        }
    }

    /**
     * Deserialize JSON string to PostCommentResult from local cache.
     */
    public PostCommentResult fromJson(String json) {
        try {
            return objectMapper.readValue(json, PostCommentResult.class);
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to deserialize PostCommentResult from JSON", e);
        }
    }
}
