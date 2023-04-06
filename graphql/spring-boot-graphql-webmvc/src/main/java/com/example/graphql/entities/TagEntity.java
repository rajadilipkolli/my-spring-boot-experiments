package com.example.graphql.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Entity
@Table(
        name = "tags",
        uniqueConstraints = {@UniqueConstraint(columnNames = "tag_name", name = "uc_tag_name")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false, unique = true, name = "tag_name")
    private String tagName;

    private String tagDescription;

    public TagEntity(String tagName, String tagDescription) {
        this.tagName = tagName;
        this.tagDescription = tagDescription;
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
