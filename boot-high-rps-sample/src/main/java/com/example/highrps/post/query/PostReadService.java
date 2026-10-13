package com.example.highrps.post.query;

import com.example.highrps.post.domain.PostRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Dedicated read service for Post aggregate JPA fallback.
 * Encapsulates the read-only database transaction and entity-to-projection mapping
 * to guarantee lazy association traversal before the transaction ends.
 */
@Service
@Transactional(readOnly = true)
public class PostReadService {

    private final PostRepository postRepository;

    public PostReadService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    /**
     * Resolves a post from the database and returns a fully mapped projection.
     *
     * @param postId the post reference identifier
     * @return optional containing the mapped projection if found
     */
    public Optional<PostProjection> findByPostRefId(Long postId) {
        return postRepository.findByPostRefId(postId).map(PostProjection::fromEntity);
    }
}
