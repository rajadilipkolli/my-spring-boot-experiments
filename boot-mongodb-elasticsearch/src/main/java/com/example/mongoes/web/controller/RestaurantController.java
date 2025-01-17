package com.example.mongoes.web.controller;

import com.example.mongoes.document.Restaurant;
import com.example.mongoes.response.GenericMessage;
import com.example.mongoes.web.api.RestaurantApi;
import com.example.mongoes.web.model.GradesRequest;
import com.example.mongoes.web.model.RestaurantRequest;
import com.example.mongoes.web.service.RestaurantService;
import io.micrometer.core.annotation.Timed;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Timed
@RequestMapping("/api/restaurant")
@Validated
class RestaurantController implements RestaurantApi {

    private final RestaurantService restaurantService;

    RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @Override
    public Mono<ResponseEntity<SearchPage<Restaurant>>> findAllRestaurants(int limit, int offset) {
        return restaurantService.findAllRestaurants(offset, limit).map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Restaurant>> findRestaurantByName(String restaurantName) {
        return restaurantService
                .findByRestaurantName(restaurantName)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Override
    public Mono<ResponseEntity<Restaurant>> findRestaurantById(Long restaurantId) {
        return restaurantService
                .findByRestaurantId(restaurantId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Override
    public Mono<ResponseEntity<Restaurant>> addGradeToRestaurant(GradesRequest request, Long id) {
        return this.restaurantService.addGrade(request, id).map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Long>> totalCount() {
        return restaurantService
                .totalCount()
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Override
    public Mono<ResponseEntity<Restaurant>> addNotesToRestaurant(
            Long restaurantId, GradesRequest grades) {
        return restaurantService.addGrade(grades, restaurantId).map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<GenericMessage>> createRestaurant(
            RestaurantRequest restaurantRequest) {
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
