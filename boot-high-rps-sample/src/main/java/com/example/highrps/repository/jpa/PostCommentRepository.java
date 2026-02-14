package com.example.highrps.repository.jpa;

import com.example.highrps.entities.PostCommentEntity;
import com.example.highrps.postcomment.domain.vo.PostCommentId;
import com.example.highrps.shared.ResourceNotFoundException;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface PostCommentRepository extends JpaRepository<PostCommentEntity, Long> {

    @Transactional
    @Modifying
    @Query("DELETE FROM PostCommentEntity pc WHERE pc.commentRefId IN :commentRefIds")
    long deleteByCommentRefIdIn(@Param("commentRefIds") List<Long> commentRefIds);

    @Query("SELECT pc FROM PostCommentEntity pc WHERE pc.postEntity.postRefId = :postId")
    List<PostCommentEntity> findByPostRefId(@Param("postId") Long postId);

    List<PostCommentEntity> findByCommentRefIdIn(List<Long> commentRefIds);

    @Query("""
            SELECT pc FROM PostCommentEntity pc
            WHERE pc.commentRefId = :id AND pc.postEntity.postRefId = :postId
            """)
    Optional<PostCommentEntity> findByCommentRefIdAndPostRefId(@Param("id") Long id, @Param("postId") Long postId);

    default PostCommentEntity getByCommentRefIdAndPostRefId(PostCommentId postCommentId, Long postId) {
        return findByCommentRefIdAndPostRefId(postCommentId.id(), postId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PostComment not found with commentRefId: " + postCommentId.id() + " for post: " + postId));
    }
}
