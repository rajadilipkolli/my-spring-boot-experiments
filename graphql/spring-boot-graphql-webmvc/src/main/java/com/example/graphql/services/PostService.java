package com.example.graphql.services;

import com.example.graphql.entities.PostEntity;
import com.example.graphql.projections.PostInfo;
import com.example.graphql.repositories.PostRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    public List<PostEntity> findAllPosts() {
        return postRepository.findAll();
    }

    public List<PostInfo> findAllPostsByAuthorEmail(String emailId) {
        return postRepository.findByAuthorEntity_EmailIgnoreCase(emailId);
    }

    public Optional<PostEntity> findPostById(Long id) {
        return postRepository.findById(id);
    }

    @Transactional
    public PostEntity savePost(PostEntity postEntity) {
        return postRepository.save(postEntity);
    }

    @Transactional
    public void deletePostById(Long id) {
        postRepository.deleteById(id);
    }

    public Map<Long, List<PostInfo>> getPostByAuthorIdIn(List<Long> authorIds) {
        return this.postRepository.findByAuthorEntity_IdIn(authorIds).stream()
                .collect(Collectors.groupingBy(postInfo -> postInfo.getAuthorEntity().getId()));
    }
}
