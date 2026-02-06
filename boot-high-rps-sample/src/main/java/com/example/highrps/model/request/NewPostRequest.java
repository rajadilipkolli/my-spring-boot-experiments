package com.example.highrps.model.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public record NewPostRequest(
        @NotBlank(message = "PostTitle must not be blank") String title,
        @NotBlank(message = "PostContent must not be blank") String content,

        @NotBlank(message = "Email must not be blank") @Email(message = "Provide valid email") String email,

        Boolean published,
        LocalDateTime publishedAt,
        @JsonIgnore LocalDateTime createdAt,
        @JsonIgnore LocalDateTime modifiedAt,
        @Valid PostDetailsRequest details,
        @Valid List<TagRequest> tags)
        implements Serializable {

    public NewPostRequest withPublishedAt(LocalDateTime now) {
        return new NewPostRequest(
                this.title,
                this.content,
                this.email,
                this.published,
                now,
                this.createdAt,
                this.modifiedAt,
                this.details,
                this.tags);
    }

    public NewPostRequest withCreatedAt(LocalDateTime now) {
        return new NewPostRequest(
                this.title,
                this.content,
                this.email,
                this.published,
                this.publishedAt,
                now,
                this.modifiedAt,
                this.details,
                this.tags);
    }

    public NewPostRequest withModifiedAt(LocalDateTime now, LocalDateTime createdAt) {
        return new NewPostRequest(
                this.title,
                this.content,
                this.email,
                this.published,
                this.publishedAt,
                createdAt,
                now,
                this.details,
                this.tags);
    }
}
