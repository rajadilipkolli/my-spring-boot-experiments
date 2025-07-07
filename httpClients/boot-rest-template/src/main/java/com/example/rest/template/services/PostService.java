package com.example.rest.template.services;

import com.example.rest.template.entities.Post;
import com.example.rest.template.model.response.PagedResult;
import com.example.rest.template.repositories.PostRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public PagedResult<Post> findAllPosts(int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort =
                sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();

        // create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<Post> postsPage = postRepository.findAll(pageable);

        return new PagedResult<>(postsPage);
    }

    public Optional<Post> findPostById(Long id) {
        return postRepository.findById(id);
    }

    @Transactional
    public Post savePost(Post post) {
        post.setId(null);
        return postRepository.save(post);
    }

    @Transactional
    public void deletePostById(Long id) {
        postRepository.deleteById(id);
    }

    @Transactional
    public Post updatePost(Post post) {
        return postRepository.save(post);
    }
}
