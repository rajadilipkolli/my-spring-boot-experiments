package com.example.highrps.post.command;

import com.example.highrps.post.domain.requests.NewPostRequest;
import com.example.highrps.post.domain.requests.PostDetailsRequest;
import com.example.highrps.post.domain.requests.TagRequest;
import java.util.List;

/**
 * Command to update an existing post.
 */
public record UpdatePostCommand(
        Long postId,
        String title,
        String content,
        Boolean published,
        PostDetailsRequest details,
        List<TagRequest> tags) {

    public UpdatePostCommand {
        if (postId == null) {
            throw new IllegalArgumentException("postId must not be null");
        }
        tags = tags == null ? List.of() : List.copyOf(tags);
    }

    public static UpdatePostCommand fromNewPostRequest(NewPostRequest newPostRequest, Long postId) {
        List<TagRequest> tagRequests = newPostRequest.tags() == null
                ? List.of()
                : newPostRequest.tags().stream()
                        .map(tag -> new TagRequest(tag.tagName(), tag.tagDescription()))
                        .toList();
        return new UpdatePostCommand(
                postId,
                newPostRequest.title(),
                newPostRequest.content(),
                newPostRequest.published(),
                new PostDetailsRequest(
                        newPostRequest.details().detailsKey(),
                        newPostRequest.details().createdBy()),
                tagRequests);
    }
}
