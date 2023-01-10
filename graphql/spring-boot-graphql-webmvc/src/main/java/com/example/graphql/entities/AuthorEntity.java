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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "authors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthorEntity {

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
    private String mobile;

    @Column(nullable = false, unique = true, length = 50)
    private String email;

    private LocalDateTime registeredAt;

    @Version private Long version;

    @OneToMany(mappedBy = "authorEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PostEntity> postEntities = new ArrayList<>();

    public void addPost(PostEntity postEntity) {
        this.postEntities.add(postEntity);
        postEntity.setAuthorEntity(this);
    }

    public void removePost(PostEntity postEntity) {
        this.postEntities.remove(postEntity);
        postEntity.setAuthorEntity(null);
    }
}
