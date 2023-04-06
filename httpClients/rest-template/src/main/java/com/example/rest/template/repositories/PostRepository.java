package com.example.rest.template.repositories;

import com.example.rest.template.entities.Post;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {}
