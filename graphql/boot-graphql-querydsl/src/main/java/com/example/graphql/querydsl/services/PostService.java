package com.example.graphql.querydsl.services;

import com.example.graphql.querydsl.entities.Post;
import com.example.graphql.querydsl.exception.PostNotFoundException;
import com.example.graphql.querydsl.mapper.PostMapper;
import com.example.graphql.querydsl.model.query.FindPostsQuery;
import com.example.graphql.querydsl.model.request.AddTagRequest;
import com.example.graphql.querydsl.model.request.CreatePostRequest;
import com.example.graphql.querydsl.model.request.UpdatePostRequest;
import com.example.graphql.querydsl.model.response.PagedResult;
import com.example.graphql.querydsl.model.response.PostResponse;
import com.example.graphql.querydsl.repositories.PostRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostMapper postMapper;

    public PagedResult<PostResponse> findAllPosts(FindPostsQuery findPostsQuery) {

        // create Pageable instance
        Pageable pageable = createPageable(findPostsQuery);

        Page<Post> postsPage = postRepository.findAll(pageable);

        List<PostResponse> postResponseList = postMapper.toResponseList(postsPage.getContent());

        return new PagedResult<>(postsPage, postResponseList);
    }

    private Pageable createPageable(FindPostsQuery findPostsQuery) {
        int pageNo = Math.max(findPostsQuery.pageNo() - 1, 0);
        Sort sort = Sort.by(
                findPostsQuery.sortDir().equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.Order.asc(findPostsQuery.sortBy())
                        : Sort.Order.desc(findPostsQuery.sortBy()));
        return PageRequest.of(pageNo, findPostsQuery.pageSize(), sort);
    }

    public Optional<PostResponse> findPostById(Long id) {
        return postRepository.findById(id).map(postMapper::toResponse);
    }

    @Transactional
    public PostResponse savePost(CreatePostRequest createPostRequest) {
        Post post = postMapper.toEntity(createPostRequest);
        Post savedPost = postRepository.save(post);
        return postMapper.toResponse(savedPost);
    }

    @Transactional
    public PostResponse updatePost(Long id, UpdatePostRequest updatePostRequest) {
        Post post = postRepository.findById(id).orElseThrow(() -> new PostNotFoundException(id));

        // Update the post object with data from postRequest
        postMapper.mapPostWithRequest(updatePostRequest, post);

        // Save the updated post object
        Post updatedPost = postRepository.save(post);

        return postMapper.toResponse(updatedPost);
    }

    @Transactional
    public void deletePostById(Long id) {
        postRepository.deleteById(id);
    }

    public Long totalPosts() {
        return postRepository.count();
    }

    public List<PostResponse> getPostsByUserName(String name) {
        List<Post> posts = postRepository.findByDetails_CreatedByEqualsIgnoreCase(name);
        if (posts.isEmpty()) {
            throw new PostNotFoundException(name);
        } else {
            // Fixing MultiBagException
            List<Post> fullyMappedPosts = postRepository.findAllPostsWithTags(posts);
            return postMapper.toResponseList(fullyMappedPosts);
        }
    }

    @Transactional
    public PostResponse addTagsToPost(AddTagRequest addTagRequest) {
        Post post = postMapper.setTags(
                addTagRequest.tagNames(), this.postRepository.getReferenceById(addTagRequest.postId()));
        return postMapper.toResponse(this.postRepository.save(post));
    }
}
