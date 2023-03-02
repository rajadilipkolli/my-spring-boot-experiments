package com.example.jooq.r2dbc.entities;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "tags")
public class Tags {

    @Id
    @Column("id")
    private UUID id;

    @Column("name")
    private String name;

    public Tags(String name) {
        this.name = name;
    }
}
