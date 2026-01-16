package com.example.highrps.entities;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(
        name = "tags",
        uniqueConstraints = {@UniqueConstraint(columnNames = "tag_name", name = "uc_tag_name")})
public class TagEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false, unique = true, name = "tag_name")
    private String tagName;

    private String tagDescription;

    public TagEntity() {}

    public TagEntity setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getId() {
        return id;
    }

    public String getTagName() {
        return tagName;
    }

    public String getTagDescription() {
        return tagDescription;
    }

    public TagEntity setTagName(String tagName) {
        this.tagName = tagName;
        return this;
    }

    public TagEntity setTagDescription(String tagDescription) {
        this.tagDescription = tagDescription;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        TagEntity other = (TagEntity) obj;
        return Objects.equals(this.tagName, other.tagName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.tagName);
    }
}
