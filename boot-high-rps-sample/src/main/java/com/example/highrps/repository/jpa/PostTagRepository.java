package com.example.highrps.repository.jpa;

import com.example.highrps.entities.PostTagEntity;
import com.example.highrps.entities.PostTagId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostTagRepository extends JpaRepository<PostTagEntity, PostTagId> {
    long countByPostEntity_Title(String title);
}
