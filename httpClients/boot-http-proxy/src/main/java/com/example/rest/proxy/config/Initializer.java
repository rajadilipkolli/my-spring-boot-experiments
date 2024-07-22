package com.example.rest.proxy.config;

import com.example.rest.proxy.client.JsonPlaceholderService;
import com.example.rest.proxy.entities.Post;
import com.example.rest.proxy.repositories.PostRepository;
import com.example.rest.proxy.utils.AppConstants;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile(AppConstants.PROFILE_NOT_TEST)
class Initializer implements CommandLineRunner {

    private final JsonPlaceholderService jsonPlaceholderService;
    private final PostRepository postRepository;

    @Override
    public void run(String... args) {
        log.info("Running Initializer.....");
        List<Post> postList = jsonPlaceholderService.loadAllPosts();
        this.postRepository.saveAll(postList);
        log.info("Completed Initializer.....");
    }
}
