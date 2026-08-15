package com.example.highrps.author.domain;

import com.example.highrps.shared.AssertUtil;
import com.example.highrps.shared.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import org.hibernate.annotations.NaturalId;

@Entity
@Table(name = "authors")
public class AuthorEntity extends BaseEntity {

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

    @NaturalId
    @Column(nullable = false, unique = true, length = 50)
    private String email;

    private LocalDateTime registeredAt;

    @Version
    @Column(nullable = false)
    private Short version;

    // Protected no-arg constructor for JPA and tests
    public AuthorEntity() {}

    // Public constructor with required fields and validation
    public AuthorEntity(String firstName, String lastName, String email, Long mobile) {
        this.firstName = AssertUtil.requireNotBlank(firstName, "First name cannot be null or empty");
        this.lastName = AssertUtil.requireNotBlank(lastName, "Last name cannot be null or empty");
        this.email = AssertUtil.requireNotBlank(email, "Email cannot be null or empty");
        this.mobile = AssertUtil.requireNotNull(mobile, "Mobile cannot be null");
        this.registeredAt = LocalDateTime.now();
    }

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

    AuthorEntity setVersion(Short version) {
        this.version = version;
        return this;
    }

    public Short getVersion() {
        return version;
    }
}
