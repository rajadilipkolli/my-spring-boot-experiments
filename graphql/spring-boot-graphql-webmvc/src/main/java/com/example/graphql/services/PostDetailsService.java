package com.example.graphql.services;

import com.example.graphql.entities.PostDetailsEntity;
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

    public List<PostDetailsEntity> findAllPostDetailss() {
        return postDetailsRepository.findAll();
    }

    public Optional<PostDetailsEntity> findPostDetailsById(Long id) {
        return postDetailsRepository.findById(id);
    }

    public PostDetailsEntity savePostDetails(PostDetailsEntity postDetailsEntity) {
        return postDetailsRepository.save(postDetailsEntity);
    }

    public void deletePostDetailsById(Long id) {
        postDetailsRepository.deleteById(id);
    }
}
