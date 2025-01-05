package com.example.learning.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record PostTagId(@Column(name = "post_id") Long postId, @Column(name = "tag_id") Long tagId) {}
