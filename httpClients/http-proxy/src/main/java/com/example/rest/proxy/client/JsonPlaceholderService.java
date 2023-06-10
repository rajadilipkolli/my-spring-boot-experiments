package com.example.rest.proxy.client;

import com.example.rest.proxy.entities.Post;
import java.util.List;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(url = "https://jsonplaceholder.typicode.com")
public interface JsonPlaceholderService {

    @GetExchange("/posts")
    List<Post> loadAllPosts();
}
