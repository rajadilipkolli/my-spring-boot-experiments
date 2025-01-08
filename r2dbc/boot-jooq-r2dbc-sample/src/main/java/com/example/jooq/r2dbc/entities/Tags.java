package com.example.jooq.r2dbc.entities;

import java.util.UUID;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(value = "tags")
public class Tags {

    @Id
    @Column("id")
    private UUID id;

    @Column("name")
    private String name;

    public Tags() {}

    public UUID getId() {
        return id;
    }

    public Tags setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Tags setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("name", name).toString();
    }
}
