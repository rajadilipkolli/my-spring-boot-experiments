package com.example.graphql.querydsl.service;

import com.example.graphql.querydsl.entities.*;
import com.example.graphql.querydsl.exception.PostNotFoundException;
import com.example.graphql.querydsl.model.PostCommentsDTO;
import com.example.graphql.querydsl.model.TagDTO;
import com.example.graphql.querydsl.model.request.AddTagRequestDTO;
import com.example.graphql.querydsl.model.request.PostRequestDTO;
import com.example.graphql.querydsl.model.response.PostResponse;
import com.example.graphql.querydsl.repository.PostRepository;
import com.example.graphql.querydsl.repository.TagRepository;
import com.querydsl.core.types.Predicate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;

    @Transactional
    @Override
    public PostResponse createPost(PostRequestDTO postRequestDTO) {
        Post post = postRequestDTOToPostIgnoringChild(postRequestDTO);
        post.addDetails(postRequestDTOToPostDetails(postRequestDTO));
        addPostCommentsToPost(postRequestDTO.comments(), post);
        addPostTagsToPost(postRequestDTO.tags(), post);
        return convertToPostDTO(postRepository.save(post));
    }

    @Override
    @Transactional
    public PostResponse addTagsToPost(AddTagRequestDTO addTagRequestDTO) {
        Post post =
                this.postRepository
                        .findById(addTagRequestDTO.postId())
                        .orElseThrow(PostNotFoundException::new);
        addPostTagsToPost(addTagRequestDTO.tagNames(), post);
        return convertToPostDTO(postRepository.save(post));
    }

    @Override
    public Long totalPosts() {
        return this.postRepository.count();
    }

    @Override
    @Transactional
    public List<PostResponse> getPostsByUserName(String name) {
        List<Post> post = this.postRepository.findByDetails_CreatedByEqualsIgnoreCase(name);
        return this.postRepository.findAllById(post.stream().map(Post::getId).toList()).stream()
                .map(this::convertToPostDTO)
                .toList();
    }

    private PostResponse convertToPostDTO(Post post) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getDetails().getCreatedBy(),
                post.getDetails().getCreatedOn(),
                getCommentsList(post.getComments()),
                getTagsList(post.getTags()));
    }

    private List<TagDTO> getTagsList(List<PostTag> tags) {
        return tags.stream()
                .map(postTag -> new TagDTO(postTag.getTag().getName()))
                .collect(Collectors.toList());
    }

    private List<PostCommentsDTO> getCommentsList(List<PostComment> comments) {
        return comments.stream()
                .map(postComment -> new PostCommentsDTO(postComment.getReview()))
                .collect(Collectors.toList());
    }

    private void addPostTagsToPost(List<TagDTO> tags, Post post) {
        if (!CollectionUtils.isEmpty(tags)) {
            tags.forEach(
                    tagDTO -> {
                        Predicate predicate = QTag.tag.name.eq(tagDTO.name());
                        Optional<Tag> tag = this.tagRepository.findOne(predicate);
                        if (tag.isPresent()) {
                            PostTag postTag = new PostTag(post, tag.get());
                            post.getTags().add(postTag);
                        } else {
                            post.addTag(tagRequestDTOToTag(tagDTO));
                        }
                    });
        }
    }

    private Tag tagRequestDTOToTag(TagDTO tagDTO) {
        Tag tag = new Tag();
        tag.setName(tagDTO.name());
        return this.tagRepository.save(tag);
    }

    private void addPostCommentsToPost(List<PostCommentsDTO> comments, Post post) {
        if (!CollectionUtils.isEmpty(comments)) {
            comments.forEach(
                    postCommentsDTO ->
                            post.addComment(postRequestDTOToPostComment(postCommentsDTO)));
        }
    }

    private PostComment postRequestDTOToPostComment(PostCommentsDTO postCommentsDTO) {
        PostComment postComment = new PostComment();
        postComment.setReview(postCommentsDTO.review());
        return postComment;
    }

    private PostDetails postRequestDTOToPostDetails(PostRequestDTO postRequestDTO) {
        PostDetails postDetails = new PostDetails();
        postDetails.setCreatedBy(postRequestDTO.name());
        return postDetails;
    }

    private Post postRequestDTOToPostIgnoringChild(PostRequestDTO postRequestDTO) {
        Post post = new Post();
        post.setTitle(postRequestDTO.title());
        post.setContent(postRequestDTO.content());
        return post;
    }
}
