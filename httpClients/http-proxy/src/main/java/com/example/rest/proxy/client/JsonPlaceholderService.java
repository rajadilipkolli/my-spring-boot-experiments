package com.example.rest.proxy.client;

import com.example.rest.proxy.entities.Post;

import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

@HttpExchange(url = "https://jsonplaceholder.typicode.com")
public interface JsonPlaceholderService {

    @GetExchange("/posts")
    List<Post> loadAllPosts();
}
