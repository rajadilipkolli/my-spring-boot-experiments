package com.example.opensearch.web.controllers;

import com.example.opensearch.entities.Restaurant;
import com.example.opensearch.model.request.RestaurantRequest;
import com.example.opensearch.model.response.PagedResult;
import com.example.opensearch.services.RestaurantService;
import com.example.opensearch.utils.AppConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping
    public PagedResult<Restaurant> getAllRestaurants(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir) {
        return restaurantService.findAllRestaurants(pageNo, pageSize, sortBy, sortDir);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Restaurant> getRestaurantById(@PathVariable String id) {
        return restaurantService
                .findRestaurantById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Restaurant createRestaurant(@RequestBody @Validated RestaurantRequest restaurantRequest) {
        return restaurantService.saveRestaurant(restaurantRequest.toRestaurant());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Restaurant> updateRestaurant(
            @PathVariable String id, @RequestBody RestaurantRequest restaurantRequest) {
        return restaurantService
                .findRestaurantById(id)
                .map(restaurantObj -> {
                    Restaurant restaurant = restaurantRequest.toRestaurant();
                    restaurant.setId(id);
                    return ResponseEntity.ok(restaurantService.saveRestaurant(restaurant));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Restaurant> deleteRestaurant(@PathVariable String id) {
        return restaurantService
                .findRestaurantById(id)
                .map(restaurant -> {
                    restaurantService.deleteRestaurantById(id);
                    return ResponseEntity.ok(restaurant);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
