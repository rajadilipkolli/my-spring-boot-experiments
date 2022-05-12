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
@Table(name = "post comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostComment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "postComment_id_generator")
    @SequenceGenerator(
            name = "postComment_id_generator",
            sequenceName = "postComment_id_seq",
            allocationSize = 100)
    private Long id;

    @Column(nullable = false)
    @NotEmpty(message = "Text cannot be empty")
    private String text;
}
