package com.example.graphql.services;

import com.example.graphql.entities.PostEntity;
import com.example.graphql.mapper.NewPostRequestToPostEntityMapper;
import com.example.graphql.model.request.NewPostRequest;
import com.example.graphql.projections.PostInfo;
import com.example.graphql.repositories.AuthorRepository;
import com.example.graphql.repositories.PostRepository;
import com.example.graphql.repositories.TagRepository;
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
    private final AuthorRepository authorRepository;
    private final TagRepository tagRepository;

    private final NewPostRequestToPostEntityMapper mapNewPostRequestToPostEntityMapper;

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

    @Transactional
    public PostEntity createPost(NewPostRequest newPostRequest) {
        PostEntity postEntity =
                this.mapNewPostRequestToPostEntityMapper.convert(newPostRequest, tagRepository);
        postEntity.setAuthorEntity(
                this.authorRepository.getReferenceByEmail(newPostRequest.email()));
        return this.postRepository.save(postEntity);
    }
}
