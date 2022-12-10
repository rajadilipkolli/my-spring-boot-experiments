package com.example.rest.proxy.repositories;

import com.example.rest.proxy.entities.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {}
