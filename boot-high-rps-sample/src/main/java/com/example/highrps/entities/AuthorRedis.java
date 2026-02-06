package com.example.highrps.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("authors:entity")
public class AuthorRedis extends Auditable implements Serializable {

    @Id
    private String email;

    private String firstName;
    private String middleName;
    private String lastName;
    private Long mobile;
    private LocalDateTime registeredAt;

    public AuthorRedis() {}

    public AuthorRedis setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public AuthorRedis setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public String getFirstName() {
        return firstName;
    }

    public AuthorRedis setMiddleName(String middleName) {
        this.middleName = middleName;
        return this;
    }

    public String getMiddleName() {
        return middleName;
    }

    public AuthorRedis setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public String getLastName() {
        return lastName;
    }

    public AuthorRedis setMobile(Long mobile) {
        this.mobile = mobile;
        return this;
    }

    public Long getMobile() {
        return mobile;
    }

    public AuthorRedis setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
        return this;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }
}
