package com.example.graphql.repositories;

import com.example.graphql.entities.PostDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostDetailsRepository extends JpaRepository<PostDetails, Long> {}
