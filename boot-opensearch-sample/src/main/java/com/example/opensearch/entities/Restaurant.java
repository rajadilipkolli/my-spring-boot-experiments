package com.example.opensearch.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "restaurants")
public class Restaurant {

    @Id
    @Field(fielddata = true, type = FieldType.Text)
    private String id;

    private String name;

    @Field(fielddata = true, type = FieldType.Text)
    private String borough;

    @Field(fielddata = true, type = FieldType.Text)
    private String cuisine;

    @Field(type = FieldType.Nested, includeInParent = true)
    private Address address;

    private List<Grades> grades = new ArrayList<>();

    public Restaurant() {}

    public Restaurant(String id, String name, String borough, String cuisine, Address address, List<Grades> grades) {
        this.id = id;
        this.name = name;
        this.borough = borough;
        this.cuisine = cuisine;
        this.address = address;
        this.grades = grades;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBorough() {
        return borough;
    }

    public void setBorough(String borough) {
        this.borough = borough;
    }

    public String getCuisine() {
        return cuisine;
    }

    public void setCuisine(String cuisine) {
        this.cuisine = cuisine;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public List<Grades> getGrades() {
        return grades;
    }

    public void setGrades(List<Grades> grades) {
        this.grades = grades;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Restaurant that = (Restaurant) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
