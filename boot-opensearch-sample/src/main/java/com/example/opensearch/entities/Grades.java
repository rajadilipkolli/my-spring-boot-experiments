package com.example.opensearch.entities;

import java.time.LocalDateTime;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;

public class Grades {

    private String grade;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @Field(
            type = FieldType.Date,
            format = {},
            pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime date;

    private Integer score;

    public Grades() {}

    public Grades(String grade, LocalDateTime date, Integer score) {
        this.grade = grade;
        this.date = date;
        this.score = score;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }
}
