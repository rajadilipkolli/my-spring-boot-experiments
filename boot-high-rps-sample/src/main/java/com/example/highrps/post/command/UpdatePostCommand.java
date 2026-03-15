package com.example.highrps.post.command;

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
    }
}
