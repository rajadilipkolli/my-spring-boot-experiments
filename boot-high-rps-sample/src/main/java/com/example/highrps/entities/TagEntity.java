package com.example.highrps.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.util.Objects;

@Entity
@Table(
        name = "tags",
        uniqueConstraints = {@UniqueConstraint(columnNames = "tag_name", name = "uc_tag_name")})
public class TagEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tags_seq")
    @SequenceGenerator(name = "tags_seq", sequenceName = "tags_seq", allocationSize = 5)
    private Long id;

    @Column(nullable = false, unique = true, name = "tag_name")
    private String tagName;

    @Column(name = "tag_description")
    private String tagDescription;

    @Version
    @Column(name = "version", nullable = false)
    private Short version;

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

    public Short getVersion() {
        return version;
    }

    public void setVersion(Short version) {
        this.version = version;
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
