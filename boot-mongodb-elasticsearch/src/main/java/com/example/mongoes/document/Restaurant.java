package com.example.mongoes.document;

import com.example.mongoes.utils.AppConstants;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.annotation.Version;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@org.springframework.data.elasticsearch.annotations.Document(
        indexName = AppConstants.RESTAURANT_COLLECTION)
@Document(collection = AppConstants.RESTAURANT_COLLECTION)
@TypeAlias(AppConstants.RESTAURANT_COLLECTION)
@Setting(shards = 3)
public class Restaurant {

    @Id private String id;

    @Indexed
    @Field(value = "restaurant_id", type = FieldType.Long)
    @org.springframework.data.mongodb.core.mapping.Field("restaurant_id")
    private Long restaurantId;

    @NotBlank(message = "Restaurant Name Can't be Blank")
    @Field(value = "restaurant_name", fielddata = true, type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Nested, includeInParent = true)
    private Address address;

    @NotBlank(message = "Borough Can't be Blank")
    @Field(fielddata = true, type = FieldType.Text)
    private String borough;

    @NotBlank(message = "Cuisine Can't be Blank")
    @Field(fielddata = true, type = FieldType.Text)
    private String cuisine;

    private List<Grades> grades = new ArrayList<>();

    @Version private Long version;

    public Restaurant() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
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

    public List<Grades> getGrades() {
        return grades;
    }

    public void setGrades(List<Grades> grades) {
        this.grades = grades;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Restaurant.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("restaurantId=" + restaurantId)
                .add("name='" + name + "'")
                .add("address=" + address)
                .add("borough='" + borough + "'")
                .add("cuisine='" + cuisine + "'")
                .add("grades=" + grades)
                .add("version=" + version)
                .toString();
    }
}
