package com.example.graphql.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "authors")
public class AuthorEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(length = 50)
    private String middleName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false, length = 15)
    private Long mobile;

    @Column(nullable = false, unique = true, length = 50)
    private String email;

    private LocalDateTime registeredAt;

    @Version
    private Short version;

    @OneToMany(mappedBy = "authorEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostEntity> postEntities = new ArrayList<>();

    public AuthorEntity setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public Long getMobile() {
        return mobile;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public Short getVersion() {
        return version;
    }

    public List<PostEntity> getPostEntities() {
        return postEntities;
    }

    public AuthorEntity setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public AuthorEntity setMiddleName(String middleName) {
        this.middleName = middleName;
        return this;
    }

    public AuthorEntity setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public AuthorEntity setMobile(Long mobile) {
        this.mobile = mobile;
        return this;
    }

    public AuthorEntity setEmail(String email) {
        this.email = email;
        return this;
    }

    public AuthorEntity setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
        return this;
    }

    public AuthorEntity setVersion(Short version) {
        this.version = version;
        return this;
    }

    public AuthorEntity setPostEntities(List<PostEntity> postEntities) {
        if (postEntities == null) {
            postEntities = new ArrayList<>();
        }
        this.postEntities = postEntities;
        return this;
    }

    public void addPost(PostEntity postEntity) {
        this.postEntities.add(postEntity);
        postEntity.setAuthorEntity(this);
    }

    public void removePost(PostEntity postEntity) {
        this.postEntities.remove(postEntity);
        postEntity.setAuthorEntity(null);
    }
}
