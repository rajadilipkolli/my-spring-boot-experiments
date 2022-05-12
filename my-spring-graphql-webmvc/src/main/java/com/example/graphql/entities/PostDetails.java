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
@Table(name = "post detailss")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "postDetails_id_generator")
    @SequenceGenerator(
            name = "postDetails_id_generator",
            sequenceName = "postDetails_id_seq",
            allocationSize = 100)
    private Long id;

    @Column(nullable = false)
    @NotEmpty(message = "Text cannot be empty")
    private String text;
}
