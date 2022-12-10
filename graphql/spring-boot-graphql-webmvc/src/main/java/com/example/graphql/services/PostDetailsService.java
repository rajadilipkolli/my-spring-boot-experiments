package com.example.graphql.services;

import com.example.graphql.entities.PostDetails;
import com.example.graphql.repositories.PostDetailsRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PostDetailsService {

    private final PostDetailsRepository postDetailsRepository;

    public List<PostDetails> findAllPostDetailss() {
        return postDetailsRepository.findAll();
    }

    public Optional<PostDetails> findPostDetailsById(Long id) {
        return postDetailsRepository.findById(id);
    }

    public PostDetails savePostDetails(PostDetails postDetails) {
        return postDetailsRepository.save(postDetails);
    }

    public void deletePostDetailsById(Long id) {
        postDetailsRepository.deleteById(id);
    }
}
