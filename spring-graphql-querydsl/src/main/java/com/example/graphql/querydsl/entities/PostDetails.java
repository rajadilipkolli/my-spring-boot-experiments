package com.example.graphql.querydsl.entities;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity(name = "PostDetails")
@Table(name = "post_details")
@Getter
@Setter
public class PostDetails {

    @Id
    @GenericGenerator(
            name = "sequenceGenerator",
            strategy = "enhanced-sequence",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled-lo"),
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "5")
            })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    private Long id;

    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @Column(name = "created_by")
    private String createdBy;

    public PostDetails() {
        this.createdOn = LocalDateTime.now();
    }

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    private Post post;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PostDetails other = (PostDetails) obj;
        return Objects.equals(this.createdBy, other.createdBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.createdBy);
    }
}