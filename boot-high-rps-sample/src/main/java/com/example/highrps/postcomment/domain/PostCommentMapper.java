package com.example.highrps.postcomment.domain;

import com.example.highrps.entities.PostCommentEntity;
import com.example.highrps.entities.PostCommentRedis;
import com.example.highrps.postcomment.command.PostCommentCommandResult;
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
    public abstract PostCommentCommandResult toResult(PostCommentEntity entity);

    public abstract List<PostCommentCommandResult> toResultList(List<PostCommentEntity> entities);

    public abstract PostCommentCommandResult toResultFromRequest(PostCommentRequest request);

    public abstract PostCommentCommandResult toResultFromRedis(PostCommentRedis redis);

    public abstract PostCommentRedis toRedis(PostCommentRequest request);

    public abstract PostCommentRequest toRequestFromResult(PostCommentCommandResult result);

    /**
     * Serialize PostCommentCommandResult to JSON string for local cache.
     */
    public String toJson(PostCommentCommandResult result) {
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to serialize PostCommentCommandResult to JSON", e);
        }
    }

    /**
     * Deserialize JSON string to PostCommentCommandResult from local cache.
     */
    public PostCommentCommandResult fromJson(String json) {
        try {
            return objectMapper.readValue(json, PostCommentCommandResult.class);
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to deserialize PostCommentCommandResult from JSON", e);
        }
    }
}
