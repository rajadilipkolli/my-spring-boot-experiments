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

    @Mapping(target = "postId", source = "postEntity.postRefId")
    @Mapping(target = "id", source = "commentRefId")
    public abstract PostCommentCommandResult toResult(PostCommentEntity entity);

    public abstract List<PostCommentCommandResult> toResultList(List<PostCommentEntity> entities);

    @Mapping(source = "commentId", target = "id")
    @Mapping(source = "postId", target = "postId")
    @Mapping(target = "publishedAt", source = "publishedAt")
    public abstract PostCommentCommandResult toResultFromRequest(PostCommentRequest request);

    @Mapping(source = "commentId", target = "id")
    @Mapping(source = "postId", target = "postId")
    @Mapping(target = "publishedAt", source = "publishedAt")
    public abstract PostCommentCommandResult toResultFromRedis(PostCommentRedis redis);

    @Mapping(source = "commentId", target = "commentId")
    @Mapping(source = "postId", target = "postId")
    public abstract PostCommentRedis toRedis(PostCommentRequest request);

    @Mapping(source = "commentRefId", target = "commentId")
    @Mapping(source = "postEntity.postRefId", target = "postId")
    public abstract PostCommentRedis toRedisFromEntity(PostCommentEntity comment);

    // Helper methods for type conversion
    protected LocalDateTime map(OffsetDateTime value) {
        return value != null ? value.toLocalDateTime() : null;
    }

    protected OffsetDateTime map(LocalDateTime value) {
        return value != null ? value.atZone(ZoneId.systemDefault()).toOffsetDateTime() : null;
    }
}
