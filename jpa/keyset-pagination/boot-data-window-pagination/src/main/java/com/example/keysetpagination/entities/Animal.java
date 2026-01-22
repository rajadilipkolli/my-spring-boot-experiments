package com.example.keysetpagination.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.Objects;
import org.hibernate.Hibernate;

@Entity
@Table(name = "animals", schema = "public")
public class Animal extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    private String habitat;

    @Version
    @Column(nullable = false)
    private Short version;

    public Animal() {}

    public Animal(Long id, String name, String type, String habitat, Short version) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.habitat = habitat;
        this.version = version;
    }

    public Long getId() {
        return id;
    }

    public Animal setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Animal setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return type;
    }

    public Animal setType(String type) {
        this.type = type;
        return this;
    }

    public String getHabitat() {
        return habitat;
    }

    public Animal setHabitat(String habitat) {
        this.habitat = habitat;
        return this;
    }

    public Short getVersion() {
        return version;
    }

    public Animal setVersion(Short version) {
        this.version = version;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Animal animal = (Animal) o;
        return id != null && Objects.equals(id, animal.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
