package com.example.mongoes.web.controller;

import com.example.mongoes.document.Restaurant;
import com.example.mongoes.response.GenericMessage;
import com.example.mongoes.web.model.GradesRequest;
import com.example.mongoes.web.model.RestaurantRequest;
import com.example.mongoes.web.service.RestaurantService;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
@Validated
class RestaurantController {

    private final RestaurantService restaurantService;

    RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping
    Mono<ResponseEntity<SearchPage<Restaurant>>> findAllRestaurants(
            @Valid @RequestParam(defaultValue = "10") @Max(999) int limit,
            @RequestParam(defaultValue = "0") int offset) {
        return restaurantService.findAllRestaurants(offset, limit).map(ResponseEntity::ok);
    }

    @GetMapping("/name/{restaurantName}")
    Mono<ResponseEntity<Restaurant>> findRestaurantByName(
            @PathVariable @NotBlank @Size(max = 255) @Pattern(regexp = "^[a-zA-Z0-9 .-]+$")
                    String restaurantName) {
        return restaurantService
                .findByRestaurantName(restaurantName)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/{restaurantId}")
    Mono<ResponseEntity<Restaurant>> findRestaurantById(@PathVariable Long restaurantId) {
        return restaurantService
                .findByRestaurantId(restaurantId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/{restaurantId}/grade")
    Mono<ResponseEntity<Restaurant>> addGradeToRestaurant(
            @RequestBody @Valid GradesRequest request, @PathVariable("restaurantId") Long id) {
        return this.restaurantService.addGrade(request, id).map(ResponseEntity::ok);
    }

    @GetMapping("/total")
    Mono<ResponseEntity<Long>> totalCount() {
        return restaurantService
                .totalCount()
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/{restaurantId}/grades/")
    Mono<ResponseEntity<Restaurant>> addNotesToRestaurant(
            @PathVariable Long restaurantId, @RequestBody @Valid GradesRequest grades) {
        return restaurantService.addGrade(grades, restaurantId).map(ResponseEntity::ok);
    }

    @PostMapping
    Mono<ResponseEntity<GenericMessage>> createRestaurant(
            @RequestBody @Valid RestaurantRequest restaurantRequest) {
        return this.restaurantService
                .createRestaurant(restaurantRequest)
                .map(
                        restaurant ->
                                ResponseEntity.created(
                                                createRestaurantUri(restaurantRequest.name()))
                                        .body(
                                                new GenericMessage(
                                                        "restaurant with name %s created"
                                                                .formatted(
                                                                        restaurantRequest
                                                                                .name()))));
    }

    private URI createRestaurantUri(String restaurantName) {
        String encodedName = URLEncoder.encode(restaurantName, StandardCharsets.UTF_8);
        return URI.create("/api/restaurant/name/%s".formatted(encodedName));
    }
}
