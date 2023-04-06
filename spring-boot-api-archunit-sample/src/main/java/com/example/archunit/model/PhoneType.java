package com.example.archunit.model;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "phoneType")
public class PhoneType extends Base {

    private static final long serialVersionUID = 1697687804373017457L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(nullable = false, length = 250)
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId(), this.getActive(), this.getVersion(), name);
    }

    @Override
    public boolean equals(Object obj) {
        return Objects.equals(this, obj);
    }
}
