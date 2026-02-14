package com.example.highrps.model.request;

import com.example.highrps.shared.IdGenerator;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public record NewPostRequest(
        Long postId,
        @NotBlank(message = "PostTitle must not be blank") String title,
        @NotBlank(message = "PostContent must not be blank") String content,

        @NotBlank(message = "Email must not be blank") @Email(message = "Provide valid email") @JsonAlias("authorEmail")
        String email,

        Boolean published,
        LocalDateTime publishedAt,
        @JsonIgnore LocalDateTime createdAt,
        @JsonIgnore LocalDateTime modifiedAt,
        @Valid @NotNull PostDetailsRequest details,
        @Valid List<TagRequest> tags)
        implements Serializable {

    public NewPostRequest withPublishedAt(LocalDateTime publishedAt) {
        return new NewPostRequest(
                this.postId,
                this.title,
                this.content,
                this.email,
                this.published,
                publishedAt,
                this.createdAt,
                this.modifiedAt,
                this.details,
                this.tags);
    }

    public NewPostRequest withTimestamps(LocalDateTime createdAt, LocalDateTime modifiedAt) {
        return new NewPostRequest(
                this.postId != null ? this.postId : IdGenerator.generateLong(),
                this.title,
                this.content,
                this.email,
                this.published,
                this.publishedAt,
                createdAt,
                modifiedAt,
                this.details,
                this.tags);
    }
}
