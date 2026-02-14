package com.example.highrps.postcomment.domain;

import com.example.highrps.entities.PostCommentEntity;
import com.example.highrps.entities.PostEntity;
import com.example.highrps.postcomment.domain.vo.PostCommentId;
import com.example.highrps.repository.jpa.PostCommentRepository;
import com.example.highrps.repository.jpa.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PostCommentCommandService {
    private final PostCommentRepository postCommentRepository;
    private final PostRepository postRepository;

    public PostCommentCommandService(PostCommentRepository postCommentRepository, PostRepository postRepository) {
        this.postCommentRepository = postCommentRepository;
        this.postRepository = postRepository;
    }

    public PostCommentId createComment(CreatePostCommentCmd cmd) {
        PostEntity post = postRepository.getReferenceById(cmd.postId());

        PostCommentEntity comment = new PostCommentEntity(cmd.title(), cmd.content(), post);

        if (Boolean.TRUE.equals(cmd.published())) {
            comment.publish();
        }

        postCommentRepository.save(comment);
        return PostCommentId.of(comment.getId());
    }

    public void updateComment(UpdatePostCommentCmd cmd) {
        PostCommentEntity comment = postCommentRepository.getByIdAndPostId(cmd.commentId(), cmd.postId());

        comment.setTitle(cmd.title());
        comment.setContent(cmd.content());

        if (Boolean.TRUE.equals(cmd.published())) {
            comment.publish();
        } else {
            comment.unpublish();
        }
    }

    public void deleteComment(PostCommentId commentId, Long postId) {
        PostCommentEntity comment = postCommentRepository.getByIdAndPostId(commentId, postId);
        postCommentRepository.delete(comment);
    }
}
