package com.example.mongoes.web.model;

import com.example.mongoes.document.Address;
import com.example.mongoes.document.Grades;
import com.example.mongoes.document.Restaurant;
import java.util.List;

public record RestaurantRequest(
        Long restaurantId,
        String name,
        String borough,
        String cuisine,
        Address address,
        List<Grades> grades) {

    public Restaurant toRestaurant() {
        Restaurant restaurant = new Restaurant();
        restaurant.setAddress(address);
        restaurant.setBorough(borough);
        restaurant.setCuisine(cuisine);
        restaurant.setGrades(grades);
        restaurant.setName(name);
        restaurant.setRestaurantId(restaurantId);
        return restaurant;
    }
}
