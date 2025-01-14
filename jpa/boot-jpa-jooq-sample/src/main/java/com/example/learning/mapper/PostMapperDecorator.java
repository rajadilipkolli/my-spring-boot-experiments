package com.example.learning.mapper;

import com.example.learning.entities.Post;
import com.example.learning.entities.Tag;
import com.example.learning.model.request.PostCommentRequest;
import com.example.learning.model.request.PostRequest;
import com.example.learning.model.request.TagRequest;
import com.example.learning.repository.TagRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class PostMapperDecorator implements PostMapper {

    @Autowired
    private TagRepository tagRepository;

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
