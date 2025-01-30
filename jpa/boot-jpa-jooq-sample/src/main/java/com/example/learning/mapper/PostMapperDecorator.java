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
        if (!CollectionUtils.isEmpty(post.getComments())) {
            if (!CollectionUtils.isEmpty(postRequest.comments())) {
                // convertPostCommentsDTO to PostComment Entities
                List<PostComment> updatePostCommentsRequest =
                        this.postMapperDelegate.postCommentsRequestListToPostCommentList(postRequest.comments());

                // Remove the existing database rows that are no
                // longer found in the incoming collection (postCommentRequested)
                List<PostComment> postCommentsToRemove = post.getComments().stream()
                        .filter(postComment -> !updatePostCommentsRequest.contains(postComment))
                        .toList();
                postCommentsToRemove.forEach(post::removeComment);

                // Update the existing database rows which can be found
                // in the incoming collection (updateCustomerRequest.getOrders())
                List<PostComment> newPostComments = updatePostCommentsRequest.stream()
                        .filter(postComment -> !post.getComments().contains(postComment))
                        .toList();

                updatePostCommentsRequest.stream()
                        .filter(postComment -> !newPostComments.contains(postComment))
                        .forEach((postComment) -> {
                            postComment.setPost(post);
                            PostComment mergedPostComment = this.postCommentRepository.save(postComment);
                            post.getComments().set(post.getComments().indexOf(mergedPostComment), mergedPostComment);
                        });

                // Add the rows found in the incoming collection,
                // which cannot be found in the current database snapshot
                newPostComments.forEach(post::addComment);
            } else {
                List<PostComment> comments = new ArrayList<>(post.getComments());
                for (PostComment comment : comments) {
                    post.removeComment(comment);
                }
            }
        } else {
            addPostCommentsToPost(postRequest.comments(), post);
        }
        if (!CollectionUtils.isEmpty(post.getTags())) {
            if (!CollectionUtils.isEmpty(postRequest.tags())) {

                // convertPostCommentsDTO to PostComment Entities
                List<Tag> updateTagsRequest = this.postMapperDelegate.tagRequestListToTagList(postRequest.tags());

                List<Tag> existingTags =
                        post.getTags().stream().map(PostTag::getTag).toList();

                // Remove the existing database rows that are no
                // longer found in the incoming collection (updateTagsRequest)
                List<Tag> tagsToRemoveList = existingTags.stream()
                        .filter(tag -> !updateTagsRequest.contains(tag))
                        .toList();
                tagsToRemoveList.forEach(post::removeTag);

                List<String> tagNames =
                        existingTags.stream().map(Tag::getTagName).toList();
                // Update the existing database rows which can be found
                // in the incoming collection (updateTagsRequest)
                List<Tag> newTagsList = updateTagsRequest.stream()
                        .filter(tag -> !tagNames.contains(tag.getTagName()))
                        .toList();

                updateTagsRequest.stream()
                        .filter(tag -> !newTagsList.contains(tag))
                        .forEach((tag) -> {
                            tag.setId(existingTags.stream()
                                    .filter(t -> t.getTagName().equals(tag.getTagName()))
                                    .findFirst()
                                    .get()
                                    .getId());
                            Tag mergedTag = this.tagRepository.save(tag);
                            PostTag mergedPostTag = new PostTag(post, mergedTag);
                            post.getTags().set(post.getTags().indexOf(mergedPostTag), mergedPostTag);
                        });

                // Add the rows found in the incoming collection,
                // which cannot be found in the current database snapshot
                newTagsList.forEach(post::addTag);

            } else {
                post.getTags().forEach(postTag -> post.removeTag(postTag.getTag()));
            }
        } else {
            addPostTagsToPost(postRequest.tags(), post);
        }
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
