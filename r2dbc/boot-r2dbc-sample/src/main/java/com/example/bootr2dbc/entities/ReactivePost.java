package com.example.bootr2dbc.entities;

import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("reactive_posts")
public class ReactivePost {
    @Id
    @Column("id")
    private Long id;

    @Column("title")
    private String title;

    @Column("content")
    private String content;

    @Column("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column("created_by")
    @CreatedBy
    private String createdBy;

    @Column("updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column("updated_by")
    @LastModifiedBy
    private String updatedBy;

    public static ReactivePostBuilder builder() {
        return new ReactivePostBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public String getContent() {
        return this.content;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public String getUpdatedBy() {
        return this.updatedBy;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    public void setCreatedAt(final LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setCreatedBy(final String createdBy) {
        this.createdBy = createdBy;
    }

    public void setUpdatedAt(final LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setUpdatedBy(final String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof ReactivePost)) {
            return false;
        } else {
            ReactivePost other = (ReactivePost) o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                Object this$id = this.getId();
                Object other$id = other.getId();
                if (this$id == null) {
                    if (other$id != null) {
                        return false;
                    }
                } else if (!this$id.equals(other$id)) {
                    return false;
                }

                Object this$title = this.getTitle();
                Object other$title = other.getTitle();
                if (this$title == null) {
                    if (other$title != null) {
                        return false;
                    }
                } else if (!this$title.equals(other$title)) {
                    return false;
                }

                Object this$content = this.getContent();
                Object other$content = other.getContent();
                if (this$content == null) {
                    if (other$content != null) {
                        return false;
                    }
                } else if (!this$content.equals(other$content)) {
                    return false;
                }

                Object this$createdAt = this.getCreatedAt();
                Object other$createdAt = other.getCreatedAt();
                if (this$createdAt == null) {
                    if (other$createdAt != null) {
                        return false;
                    }
                } else if (!this$createdAt.equals(other$createdAt)) {
                    return false;
                }

                Object this$createdBy = this.getCreatedBy();
                Object other$createdBy = other.getCreatedBy();
                if (this$createdBy == null) {
                    if (other$createdBy != null) {
                        return false;
                    }
                } else if (!this$createdBy.equals(other$createdBy)) {
                    return false;
                }

                Object this$updatedAt = this.getUpdatedAt();
                Object other$updatedAt = other.getUpdatedAt();
                if (this$updatedAt == null) {
                    if (other$updatedAt != null) {
                        return false;
                    }
                } else if (!this$updatedAt.equals(other$updatedAt)) {
                    return false;
                }

                Object this$updatedBy = this.getUpdatedBy();
                Object other$updatedBy = other.getUpdatedBy();
                if (this$updatedBy == null) {
                    if (other$updatedBy != null) {
                        return false;
                    }
                } else if (!this$updatedBy.equals(other$updatedBy)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(final Object other) {
        return other instanceof ReactivePost;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Object $id = this.getId();
        result = result * 59 + ($id == null ? 43 : $id.hashCode());
        Object $title = this.getTitle();
        result = result * 59 + ($title == null ? 43 : $title.hashCode());
        Object $content = this.getContent();
        result = result * 59 + ($content == null ? 43 : $content.hashCode());
        Object $createdAt = this.getCreatedAt();
        result = result * 59 + ($createdAt == null ? 43 : $createdAt.hashCode());
        Object $createdBy = this.getCreatedBy();
        result = result * 59 + ($createdBy == null ? 43 : $createdBy.hashCode());
        Object $updatedAt = this.getUpdatedAt();
        result = result * 59 + ($updatedAt == null ? 43 : $updatedAt.hashCode());
        Object $updatedBy = this.getUpdatedBy();
        result = result * 59 + ($updatedBy == null ? 43 : $updatedBy.hashCode());
        return result;
    }

    public String toString() {
        Long var10000 = this.getId();
        return "ReactivePost(id="
                + var10000
                + ", title="
                + this.getTitle()
                + ", content="
                + this.getContent()
                + ", createdAt="
                + String.valueOf(this.getCreatedAt())
                + ", createdBy="
                + this.getCreatedBy()
                + ", updatedAt="
                + String.valueOf(this.getUpdatedAt())
                + ", updatedBy="
                + this.getUpdatedBy()
                + ")";
    }

    public ReactivePost(
            final Long id,
            final String title,
            final String content,
            final LocalDateTime createdAt,
            final String createdBy,
            final LocalDateTime updatedAt,
            final String updatedBy) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public ReactivePost() {}

    public static class ReactivePostBuilder {

        private Long id;

        private String title;

        private String content;

        private LocalDateTime createdAt;

        private String createdBy;

        private LocalDateTime updatedAt;

        private String updatedBy;

        ReactivePostBuilder() {}

        public ReactivePostBuilder id(final Long id) {
            this.id = id;
            return this;
        }

        public ReactivePostBuilder title(final String title) {
            this.title = title;
            return this;
        }

        public ReactivePostBuilder content(final String content) {
            this.content = content;
            return this;
        }

        public ReactivePostBuilder createdAt(final LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ReactivePostBuilder createdBy(final String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public ReactivePostBuilder updatedAt(final LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public ReactivePostBuilder updatedBy(final String updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        public ReactivePost build() {
            return new ReactivePost(
                    this.id,
                    this.title,
                    this.content,
                    this.createdAt,
                    this.createdBy,
                    this.updatedAt,
                    this.updatedBy);
        }

        public String toString() {
            Long var10000 = this.id;
            return "ReactivePost.ReactivePostBuilder(id="
                    + var10000
                    + ", title="
                    + this.title
                    + ", content="
                    + this.content
                    + ", createdAt="
                    + String.valueOf(this.createdAt)
                    + ", createdBy="
                    + this.createdBy
                    + ", updatedAt="
                    + String.valueOf(this.updatedAt)
                    + ", updatedBy="
                    + this.updatedBy
                    + ")";
        }
    }
}
