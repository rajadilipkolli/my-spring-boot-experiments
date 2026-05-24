package com.example.grpc.spring.repositories;

import com.example.grpc.spring.entities.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<PostEntity, Long> {}
