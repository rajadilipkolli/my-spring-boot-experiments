package com.example.rest.webclient.repositories;

import com.example.rest.webclient.entities.Post;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {}
