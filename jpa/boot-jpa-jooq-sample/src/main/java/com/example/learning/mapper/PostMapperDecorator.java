package com.example.learning.mapper;

import com.example.learning.entities.Post;
import com.example.learning.entities.Tag;
import com.example.learning.model.request.PostCommentRequest;
import com.example.learning.model.request.PostRequest;
import com.example.learning.model.request.TagRequest;
import com.example.learning.repository.TagRepository;
import java.util.List;
import java.util.Optional;
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

        tagRequests.forEach(tagRequest -> {
            // Check if the tag already exists
            Optional<Tag> tagOptional = this.tagRepository.findByTagName(tagRequest.name());
            Tag tag = tagOptional.orElseGet(() -> tagRequestToTag(tagRequest));
            // Use the managed Tag entity to create and associate PostTag
            post.addTag(tag);
        });
    }

    private void addPostCommentsToPost(List<PostCommentRequest> comments, Post post) {
        if (comments == null) {
            return;
        }

        comments.forEach(comment -> post.addComment(postCommentRequestToPostComment(comment)));
    }
}
