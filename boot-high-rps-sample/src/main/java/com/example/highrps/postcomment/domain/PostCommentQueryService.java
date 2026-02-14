package com.example.highrps.postcomment.domain;

import com.example.highrps.entities.PostCommentEntity;
import com.example.highrps.repository.jpa.PostCommentRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PostCommentQueryService {
    private final PostCommentRepository postCommentRepository;
    private final PostCommentMapper postCommentMapper;

    public PostCommentQueryService(PostCommentRepository postCommentRepository, PostCommentMapper postCommentMapper) {
        this.postCommentRepository = postCommentRepository;
        this.postCommentMapper = postCommentMapper;
    }

    public List<PostCommentResult> getCommentsByPostId(Long postId) {
        List<PostCommentEntity> byPostId = postCommentRepository.findByPostId(postId);
        if (byPostId.isEmpty()) {
            return List.of();
        } else {
            return postCommentMapper.toResultList(byPostId);
        }
    }

    public PostCommentResult getCommentById(GetPostCommentQuery query) {
        var comment = postCommentRepository.getByIdAndPostId(query.commentId(), query.postId());
        return postCommentMapper.toResult(comment);
    }
}
