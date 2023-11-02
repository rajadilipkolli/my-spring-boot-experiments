package com.example.opensearch.model.request;

import com.example.opensearch.entities.Address;
import com.example.opensearch.entities.Grades;
import com.example.opensearch.entities.Restaurant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record RestaurantRequest(
        @NotEmpty(message = "Name cannot be empty") String name,
        @NotBlank(message = "Borough Can't be Blank") String borough,
        @NotBlank(message = "Cuisine Can't be Blank") String cuisine,
        Address address,
        List<Grades> grades) {

    public Restaurant toRestaurant() {
        return new Restaurant(null, name, borough, cuisine, address, grades);
    }
}
