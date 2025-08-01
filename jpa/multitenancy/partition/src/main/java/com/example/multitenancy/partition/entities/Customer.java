package com.example.multitenancy.partition.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import org.hibernate.annotations.TenantId;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    @NotEmpty(message = "Text cannot be empty") private String text;

    @Column(nullable = false)
    @TenantId
    private String tenant;

    public Customer(Long id, String text) {
        this.id = id;
        this.text = text;
    }

    public Customer() {}

    public Long getId() {
        return id;
    }

    public Customer setId(Long id) {
        this.id = id;
        return this;
    }

    public String getText() {
        return text;
    }

    public Customer setText(String text) {
        this.text = text;
        return this;
    }

    public String getTenant() {
        return tenant;
    }

    public Customer setTenant(String tenant) {
        this.tenant = tenant;
        return this;
    }
}
