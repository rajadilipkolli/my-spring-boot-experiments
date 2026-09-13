package com.example.highrps.postcomment.domain;

import com.example.highrps.postcomment.command.PostCommentCommandResult;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class PostCommentMapper {

    /**
     * Maps a persisted comment to a command result.
     *
     * @param entity the persisted comment
     * @return the mapped result
     */
    @Mapping(target = "postId", source = "postEntity.postRefId")
    @Mapping(target = "id", source = "commentRefId")
    public abstract PostCommentCommandResult toResult(PostCommentEntity entity);

    /**
     * Maps persisted comments to command results.
     *
     * @param entities the persisted comments
     * @return the mapped results
     */
    public abstract List<PostCommentCommandResult> toResultList(List<PostCommentEntity> entities);

    /**
     * Maps a comment request to a command result.
     *
     * @param request the comment request
     * @return the mapped result
     */
    @Mapping(source = "commentId", target = "id")
    @Mapping(source = "postId", target = "postId")
    @Mapping(target = "publishedAt", source = "publishedAt")
    public abstract PostCommentCommandResult toResultFromRequest(PostCommentRequest request);

    /**
     * Maps a Redis comment to a command result.
     *
     * @param redis the cached comment
     * @return the mapped result
     */
    @Mapping(source = "commentId", target = "id")
    @Mapping(source = "postId", target = "postId")
    @Mapping(target = "publishedAt", source = "publishedAt")
    public abstract PostCommentCommandResult toResultFromRedis(PostCommentRedis redis);

    /**
     * Maps a comment request to its Redis representation.
     *
     * @param request the comment request
     * @return the Redis representation
     */
    @Mapping(source = "commentId", target = "commentId")
    @Mapping(source = "postId", target = "postId")
    public abstract PostCommentRedis toRedis(PostCommentRequest request);

    /**
     * Maps a persisted comment to its Redis representation.
     *
     * @param comment the persisted comment
     * @return the Redis representation
     */
    @Mapping(source = "commentRefId", target = "commentId")
    @Mapping(source = "postEntity.postRefId", target = "postId")
    public abstract PostCommentRedis toRedisFromEntity(PostCommentEntity comment);

    /**
     * Converts an offset timestamp to a local timestamp.
     *
     * @param value the timestamp to convert
     * @return the local timestamp, or {@code null}
     */
    protected LocalDateTime map(OffsetDateTime value) {
        return value != null ? value.toLocalDateTime() : null;
    }

    /**
     * Converts a local timestamp to the system-zone offset.
     *
     * @param value the timestamp to convert
     * @return the offset timestamp, or {@code null}
     */
    protected OffsetDateTime map(LocalDateTime value) {
        return value != null ? value.atZone(ZoneId.systemDefault()).toOffsetDateTime() : null;
    }
}
