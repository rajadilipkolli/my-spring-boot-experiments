package com.example.highrps.post.command;

import com.example.highrps.post.domain.requests.NewPostRequest;
import com.example.highrps.post.domain.requests.PostDetailsRequest;
import com.example.highrps.post.domain.requests.TagRequest;
import com.example.highrps.shared.IdGenerator;
import java.util.List;

/**
 * Command to create a new post.
 */
public record CreatePostCommand(
        Long postId,
        String title,
        String content,
        String authorEmail,
        Boolean published,
        PostDetailsRequest details,
        List<TagRequest> tags) {

    public CreatePostCommand {
        if (postId == null) {
            throw new IllegalArgumentException("postId must not be null");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content must not be blank");
        }
        if (authorEmail == null || authorEmail.isBlank()) {
            throw new IllegalArgumentException("authorEmail must not be blank");
        }
        tags = tags == null ? List.of() : List.copyOf(tags);
    }

    public static CreatePostCommand fromNewPostRequest(NewPostRequest newPostRequest) {

        List<TagRequest> tagRequests = newPostRequest.tags() == null
                ? List.of()
                : newPostRequest.tags().stream()
                        .map(tag -> new TagRequest(tag.tagName(), tag.tagDescription()))
                        .toList();
        return new CreatePostCommand(
                newPostRequest.postId() != null ? newPostRequest.postId() : IdGenerator.generateLong(),
                newPostRequest.title(),
                newPostRequest.content(),
                newPostRequest.email(),
                newPostRequest.published(),
                new PostDetailsRequest(
                        newPostRequest.details().detailsKey(),
                        newPostRequest.details().createdBy()),
                tagRequests);
    }
}
