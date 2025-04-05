package com.example.mongoes.web.controller;

import com.example.mongoes.document.Restaurant;
import com.example.mongoes.model.request.GradesRequest;
import com.example.mongoes.model.request.RestaurantRequest;
import com.example.mongoes.web.api.RestaurantApi;
import com.example.mongoes.web.service.RestaurantService;
import io.micrometer.core.annotation.Timed;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
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
        return restaurantService.findByRestaurantName(restaurantName).map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Restaurant>> findRestaurantById(Long restaurantId) {
        return restaurantService.findByRestaurantId(restaurantId).map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Restaurant>> addGradeToRestaurant(GradesRequest request, Long id) {
        return this.restaurantService.addGrade(request, id).map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Long>> totalCount() {
        return restaurantService.totalCount().defaultIfEmpty(0L).map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Restaurant>> updateGradesOfRestaurant(
            Long restaurantId, List<GradesRequest> gradesRequestList) {
        return restaurantService
                .updateGrades(gradesRequestList, restaurantId)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Void>> createRestaurant(RestaurantRequest restaurantRequest) {
        return this.restaurantService
                .createRestaurant(restaurantRequest)
                .map(
                        restaurant ->
                                ResponseEntity.created(
                                                createRestaurantUri(restaurantRequest.name()))
                                        .build());
    }

    private URI createRestaurantUri(String restaurantName) {
        String encodedName = URLEncoder.encode(restaurantName, StandardCharsets.UTF_8);
        return URI.create("/api/restaurant/name/%s".formatted(encodedName));
    }
}
