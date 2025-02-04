package com.example.learning.mapper;

import com.example.learning.entities.Post;
import com.example.learning.entities.PostComment;
import com.example.learning.entities.PostTag;
import com.example.learning.entities.Tag;
import com.example.learning.model.request.PostCommentRequest;
import com.example.learning.model.request.PostRequest;
import com.example.learning.model.request.TagRequest;
import com.example.learning.repository.PostCommentRepository;
import com.example.learning.repository.TagRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.CollectionUtils;

public abstract class PostMapperDecorator implements PostMapper {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private PostCommentRepository postCommentRepository;

    @Autowired
    @Qualifier("delegate") private PostMapper postMapperDelegate;

    @Override
    public Post postRequestToEntity(PostRequest postRequest, String userName) {
        if (postRequest == null) {
            return null;
        }

        Post post = postDtoToPostIgnoringChild(postRequest, userName);
        addPostCommentsToPost(postRequest.comments(), post);
        addPostTagsToPost(postRequest.tags(), post);

        return post;
    }

    @Override
    public void updateReferenceValues(PostRequest postRequest, Post post) {
        if (postRequest == null) {
            return;
        }

        post.setTitle(postRequest.title());
        post.setContent(postRequest.content());
        post.setPublished(postRequest.published());
        post.setPublishedAt(postRequest.publishedAt());
        updateComments(postRequest, post);
        updateTags(postRequest, post);
    }

    private void updateTags(PostRequest postRequest, Post post) {
        if (CollectionUtils.isEmpty(postRequest.tags())) {
            new ArrayList<>(post.getTags()).forEach(postTag -> post.removeTag(postTag.getTag()));
            return;
        }

        List<Tag> updateTagsRequest = this.postMapperDelegate.tagRequestListToTagList(postRequest.tags());
        List<Tag> existingTags = post.getTags().stream().map(PostTag::getTag).toList();

        Map<String, Tag> existingTagMap =
                existingTags.stream().collect(Collectors.toMap(Tag::getTagName, Function.identity()));

        List<Tag> newTags = new ArrayList<>();
        for (Tag tag : updateTagsRequest) {
            if (!existingTagMap.containsKey(tag.getTagName())) {
                newTags.add(tag);
            } else {
                tag.setId(existingTagMap.get(tag.getTagName()).getId());
                Tag mergedTag = this.tagRepository.save(tag);
                PostTag mergedPostTag = new PostTag(post, mergedTag);
                post.getTags().set(post.getTags().indexOf(mergedPostTag), mergedPostTag);
            }
        }

        List<Tag> tagsToRemove = existingTags.stream()
                .filter(tag -> !updateTagsRequest.contains(tag))
                .toList();
        tagsToRemove.forEach(post::removeTag);

        if (!newTags.isEmpty()) {
            tagRepository.saveAll(newTags).forEach(post::addTag);
        }
    }

    private void updateComments(PostRequest postRequest, Post post) {
        List<PostComment> existingComments = new ArrayList<>(post.getComments());
        List<PostComment> updatedComments =
                this.postMapperDelegate.postCommentsRequestListToPostCommentList(postRequest.comments());

        // Remove comments that are not in the updated request
        existingComments.stream()
                .filter(comment -> !updatedComments.contains(comment))
                .forEach(post::removeComment);

        // Update existing comments and add new ones
        updatedComments.forEach(comment -> {
            comment.setPost(post);
            if (existingComments.contains(comment)) {
                PostComment mergedComment = this.postCommentRepository.save(comment);
                post.getComments().set(post.getComments().indexOf(mergedComment), mergedComment);
            } else {
                post.addComment(comment);
            }
        });
    }

    private void addPostTagsToPost(List<TagRequest> tagRequests, Post post) {
        if (tagRequests == null) {
            return;
        }

        Map<String, TagRequest> newTagRequests =
                tagRequests.stream().collect(Collectors.toMap(TagRequest::name, Function.identity()));

        // Find existing tags in a single query
        List<Tag> existingTags = tagRepository.findByTagNameIn(newTagRequests.keySet());
        Map<String, Tag> tagMap = existingTags.stream().collect(Collectors.toMap(Tag::getTagName, Function.identity()));

        // Create new tags in batch
        List<Tag> newTags = newTagRequests.entrySet().stream()
                .filter(e -> !tagMap.containsKey(e.getKey()))
                .map(e -> tagRequestToTag(e.getValue()))
                .collect(Collectors.toList());
        if (!newTags.isEmpty()) {
            tagRepository.saveAll(newTags);
            newTags.forEach(tag -> tagMap.put(tag.getTagName(), tag));
        }

        // Associate all tags with the post
        tagRequests.forEach(tagRequest -> post.addTag(tagMap.get(tagRequest.name())));
    }

    private void addPostCommentsToPost(List<PostCommentRequest> comments, Post post) {
        if (comments == null) {
            return;
        }

        comments.forEach(comment -> post.addComment(postCommentRequestToPostComment(comment)));
    }
}
