package com.example.highrps.postcomment.domain;

import com.example.highrps.entities.PostCommentEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PostCommentMapper {

    @Mapping(target = "postId", source = "postEntity.id")
    PostCommentResult toResult(PostCommentEntity entity);

    List<PostCommentResult> toResultList(List<PostCommentEntity> entities);
}
