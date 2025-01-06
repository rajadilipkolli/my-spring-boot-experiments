package com.example.learning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.all;
import static org.instancio.Select.field;

import com.example.learning.common.AbstractIntegrationTest;
import com.example.learning.entities.Post;
import com.example.learning.entities.PostComment;
import com.example.learning.entities.PostDetails;
import com.example.learning.entities.PostTag;
import com.example.learning.entities.Tag;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

class ApplicationIntegrationTest extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
        // Create a finite dataset of tags with unique tag names
        List<Tag> finiteTags = Instancio.ofList(Tag.class)
                .size(100) // Limit to 100 unique tags
                .ignore(field(Tag::getId))
                .create();

        // Generate Post objects with specific fields ignored
        List<Post> postList = Instancio.ofList(Post.class)
                .size(100)
                .ignore(field(Post::getId))
                .ignore(field(Post::getCreatedAt))
                .ignore(field(Post::getModifiedAt))
                .supply(all(LocalDateTime.class), () -> LocalDateTime.now())
                .supply(field(Post::getDetails), () -> Instancio.of(PostDetails.class)
                        .ignore(field(PostDetails::getId))
                        .ignore(field(PostDetails::getCreatedAt))
                        .ignore(field(PostDetails::getModifiedAt))
                        .ignore(field(PostDetails::getPost))
                        .create())
                .supply(field(Post::getComments), () -> new ArrayList<PostComment>())
                .supply(field(Post::getTags), () -> new ArrayList<PostTag>())
                .create();

        // Assign tags and comments to posts, reusing finite tags
        postList.forEach(post -> {
            post.setDetails(post.getDetails());

            List<PostComment> postCommentList = Instancio.ofList(PostComment.class)
                    .size(50) // Assuming 50 comments per post
                    .ignore(field(PostComment::getId))
                    .ignore(field(PostComment::getCreatedAt))
                    .ignore(field(PostComment::getModifiedAt))
                    .ignore(field(PostComment::getPost))
                    .create();
            postCommentList.forEach(post::addComment);

            // Randomly select tags from the finiteTags dataset
            Collections.shuffle(finiteTags); // Shuffle to randomize selection
            List<Tag> tags = finiteTags.stream()
                    .limit(30) // Assign 30 tags per post
                    .toList();
            tags.forEach(post::addTag);
        });

        // Save the modified posts
        List<Post> savedPostList = postRepository.saveAll(postList);

        // Assertions to validate the results
        assertThat(savedPostList).isNotEmpty().hasSize(postList.size());
        assertThat(tagRepository.count()).isEqualTo(100);
    }
}
