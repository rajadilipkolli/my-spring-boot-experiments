package com.example.archunit.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "emailType")
public class EmailType extends Base {

    @Column(nullable = false, length = 250)
    private String name;

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
