package com.example.highrps.post.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PostTagRepository extends JpaRepository<PostTagEntity, PostTagId> {
    long countByPostEntity_Title(String title);
}
