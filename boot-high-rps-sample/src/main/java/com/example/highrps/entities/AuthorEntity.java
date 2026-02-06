package com.example.highrps.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "authors")
public class AuthorEntity extends Auditable {

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
    @Column(nullable = false)
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

    public AuthorEntity setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public String getFirstName() {
        return firstName;
    }

    public AuthorEntity setMiddleName(String middleName) {
        this.middleName = middleName;
        return this;
    }

    public String getMiddleName() {
        return middleName;
    }

    public AuthorEntity setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public String getLastName() {
        return lastName;
    }

    public AuthorEntity setMobile(Long mobile) {
        this.mobile = mobile;
        return this;
    }

    public Long getMobile() {
        return mobile;
    }

    public AuthorEntity setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public AuthorEntity setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
        return this;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public AuthorEntity setVersion(Short version) {
        this.version = version;
        return this;
    }

    public Short getVersion() {
        return version;
    }

    public AuthorEntity setPostEntities(@Nullable List<PostEntity> postEntities) {
        if (postEntities == null) {
            postEntities = new ArrayList<>();
        }
        this.postEntities = postEntities;
        return this;
    }

    public List<PostEntity> getPostEntities() {
        return postEntities;
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
