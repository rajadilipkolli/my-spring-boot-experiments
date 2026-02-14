package com.example.highrps.repository.jpa;

import com.example.highrps.entities.PostCommentEntity;
import com.example.highrps.postcomment.domain.vo.PostCommentId;
import com.example.highrps.shared.ResourceNotFoundException;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostCommentRepository extends JpaRepository<PostCommentEntity, Long> {

    @Query("SELECT pc FROM PostCommentEntity pc WHERE pc.postEntity.id = :postId")
    List<PostCommentEntity> findByPostId(@Param("postId") Long postId);

    @Query("""
            SELECT pc FROM PostCommentEntity pc
            WHERE pc.id = :id AND pc.postEntity.id = :postId
            """)
    Optional<PostCommentEntity> findByIdAndPostId(@Param("id") Long id, @Param("postId") Long postId);

    default PostCommentEntity getByIdAndPostId(PostCommentId postCommentId, Long postId) {
        return findByIdAndPostId(postCommentId.id(), postId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PostComment not found with id: " + postCommentId.id() + " for post: " + postId));
    }
}
