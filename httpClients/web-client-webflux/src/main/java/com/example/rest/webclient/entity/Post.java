package com.example.rest.webclient.entity;

import lombok.Data;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("posts")
public class Post {

    @Id private Long id;

    private String title;

    @Column("user_id")
    private Long userId;

    private String body;
}
