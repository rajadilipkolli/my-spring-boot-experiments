package com.example.highrps.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public record PostTagId(
        @Column(name = "post_id") Long postId,
        @Column(name = "tag_id") Long tagId) implements Serializable {}
