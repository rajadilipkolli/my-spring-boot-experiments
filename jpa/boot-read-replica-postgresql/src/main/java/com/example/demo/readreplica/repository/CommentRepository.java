package com.example.demo.readreplica.repository;

import com.example.demo.readreplica.entities.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {}
