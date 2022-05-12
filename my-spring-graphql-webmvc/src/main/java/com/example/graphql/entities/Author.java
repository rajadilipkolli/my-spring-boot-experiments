package com.example.graphql.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "authors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "author_id_generator")
    @SequenceGenerator(
            name = "author_id_generator",
            sequenceName = "author_id_seq",
            allocationSize = 100)
    private Long id;

    @Column(nullable = false)
    @NotEmpty(message = "Text cannot be empty")
    private String text;
}
