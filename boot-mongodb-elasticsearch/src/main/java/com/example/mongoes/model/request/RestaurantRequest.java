package com.example.mongoes.model.request;

import com.example.mongoes.document.Address;
import com.example.mongoes.document.Restaurant;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record RestaurantRequest(
        Long restaurantId,
        @NotBlank(message = "Name cannot be blank") String name,
        @NotBlank(message = "Borough cannot be blank") String borough,
        @NotBlank(message = "Cuisine cannot be blank") String cuisine,
        Address address,
        @Valid List<GradesRequest> grades) {

    public Restaurant toRestaurant() {
        Restaurant restaurant = new Restaurant();
        restaurant.setAddress(address);
        restaurant.setBorough(borough);
        restaurant.setCuisine(cuisine);
        restaurant.setGrades(grades.stream().map(GradesRequest::toGrade).toList());
        restaurant.setName(name);
        restaurant.setRestaurantId(restaurantId);
        return restaurant;
    }
}
