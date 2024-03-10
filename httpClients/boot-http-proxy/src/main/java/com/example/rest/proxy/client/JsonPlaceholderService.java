package com.example.rest.proxy.client;

import com.example.rest.proxy.entities.Post;
import com.example.rest.proxy.model.response.PostCommentDto;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

public interface JsonPlaceholderService {

    @GetExchange("/posts")
    List<Post> loadAllPosts();

    @GetExchange("/posts/{id}")
    Post loadPostById(@PathVariable Long id);

    @GetExchange("/posts/{id}/comments")
    List<PostCommentDto> loadPostCommentsById(@PathVariable Long id);

    @PostExchange("/posts")
    Post createPost(@RequestBody Post post);

    @PutExchange("/posts/{id}")
    Post updatePostById(@PathVariable Long id, @RequestBody Post post);

    @DeleteExchange("/posts/{id}")
    void deletePostById(@PathVariable Long id);
}
