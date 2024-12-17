package com.example.keysetpagination.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;

@Entity
@Table(name = "animals", schema = "public")
@Getter
@NoArgsConstructor
@AllArgsConstructor
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
    private Short version;

    public Animal setId(Long id) {
        this.id = id;
        return this;
    }

    public Animal setName(String name) {
        this.name = name;
        return this;
    }

    public Animal setType(String type) {
        this.type = type;
        return this;
    }

    public Animal setHabitat(String habitat) {
        this.habitat = habitat;
        return this;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
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
