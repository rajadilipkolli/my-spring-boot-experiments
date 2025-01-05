package com.example.mongoes.web.controller;

import com.example.mongoes.document.Grades;
import com.example.mongoes.document.Restaurant;
import com.example.mongoes.response.GenericMessage;
import com.example.mongoes.web.model.RestaurantRequest;
import com.example.mongoes.web.service.RestaurantService;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Timed
@RequestMapping("/api/restaurant")
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping
    public Mono<ResponseEntity<SearchPage<Restaurant>>> findAllRestaurants(
            @Valid @RequestParam(defaultValue = "10") @Size(max = 999) int limit,
            @RequestParam(defaultValue = "0") int offset) {
        return restaurantService.findAllRestaurants(offset, limit).map(ResponseEntity::ok);
    }

    @GetMapping("/name/{restaurantName}")
    public Mono<ResponseEntity<Restaurant>> findRestaurantByName(
            @PathVariable String restaurantName) {
        return restaurantService
                .findByRestaurantName(restaurantName)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/{restaurantId}")
    public Mono<ResponseEntity<Restaurant>> findRestaurantById(@PathVariable Long restaurantId) {
        return restaurantService
                .findByRestaurantId(restaurantId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/{restaurantId}/grade")
    public Mono<Restaurant> addGradeToRestaurant(
            @RequestBody Grades request, @PathVariable("restaurantId") Long id) {
        return this.restaurantService.addGrade(request, id);
    }

    @GetMapping("/total")
    public Mono<ResponseEntity<Long>> totalCount() {
        return restaurantService
                .totalCount()
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/{restaurantId}/grades/")
    public Mono<ResponseEntity<Restaurant>> addNotesToRestaurant(
            @PathVariable Long restaurantId, @RequestBody Grades grades) {
        return restaurantService.addGrade(grades, restaurantId).map(ResponseEntity::ok);
    }

    @PostMapping
    public Mono<ResponseEntity<GenericMessage>> createRestaurant(
            @RequestBody @Valid RestaurantRequest restaurantRequest) {
        return this.restaurantService
                .createRestaurant(restaurantRequest)
                .map(
                        restaurant ->
                                ResponseEntity.created(
                                                URI.create(
                                                        "/api/restaurant/name/%s"
                                                                .formatted(
                                                                        URLEncoder.encode(
                                                                                restaurantRequest
                                                                                        .name(),
                                                                                StandardCharsets
                                                                                        .UTF_8))))
                                        .body(
                                                new GenericMessage(
                                                        "restaurant with name %s created"
                                                                .formatted(
                                                                        restaurantRequest
                                                                                .name()))));
    }
}
