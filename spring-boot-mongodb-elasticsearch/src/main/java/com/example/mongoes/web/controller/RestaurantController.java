package com.example.mongoes.web.controller;

import com.example.mongoes.mongodb.document.Grades;
import com.example.mongoes.mongodb.document.Restaurant;
import com.example.mongoes.web.service.RestaurantService;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Timed
@RequestMapping("/restaurant")
public class RestaurantController {

    private final RestaurantService restaurantService;

    @GetMapping("/name/{restaurantName}")
    public Mono<ResponseEntity<Restaurant>> findRestaurantByName(
            @PathVariable("restaurantName") String restaurantName) {
        return restaurantService
                .findByRestaurantName(restaurantName)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Restaurant>> findRestaurantById(
            @PathVariable("id") Long restaurantId) {
        return restaurantService
                .findByRestaurantId(restaurantId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/grade")
    public Mono<Restaurant> addGradeToRestaurant(
            @RequestBody Grades request, @PathVariable Long id) {
        return this.restaurantService.addGrade(request, id);
    }

    @GetMapping("/total")
    public Mono<ResponseEntity<Long>> totalCount() {
        return restaurantService
                .totalCount()
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
