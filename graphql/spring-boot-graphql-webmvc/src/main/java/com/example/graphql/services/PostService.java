package com.example.graphql.services;

import com.example.graphql.entities.Post;
import com.example.graphql.repositories.PostRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    public List<Post> findAllPosts() {
        return postRepository.findAll();
    }

    public Optional<Post> findPostById(Long id) {
        return postRepository.findById(id);
    }

    public Post savePost(Post post) {
        return postRepository.save(post);
    }

    public void deletePostById(Long id) {
        postRepository.deleteById(id);
    }
}
