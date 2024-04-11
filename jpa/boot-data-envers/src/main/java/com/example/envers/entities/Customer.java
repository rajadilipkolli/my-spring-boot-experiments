package com.example.envers.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "customers")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Audited
@ToString
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String address;

    @Version
    Short version;

    public Customer setId(Long id) {
        this.id = id;
        return this;
    }

    public Customer setName(String name) {
        this.name = name;
        return this;
    }

    public Customer setAddress(String address) {
        this.address = address;
        return this;
    }

    public Customer setVersion(Short version) {
        this.version = version;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Customer customer = (Customer) o;
        return id != null && Objects.equals(id, customer.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
