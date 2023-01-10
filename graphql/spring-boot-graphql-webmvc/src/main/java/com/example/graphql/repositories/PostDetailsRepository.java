package com.example.graphql.repositories;

import com.example.graphql.entities.PostDetailsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostDetailsRepository extends JpaRepository<PostDetailsEntity, Long> {}
