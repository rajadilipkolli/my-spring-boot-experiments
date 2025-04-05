package com.example.mongoes.web.service;

import com.example.mongoes.document.Restaurant;
import com.example.mongoes.model.request.GradesRequest;
import com.example.mongoes.model.request.RestaurantRequest;
import com.example.mongoes.repository.elasticsearch.RestaurantESRepository;
import com.example.mongoes.repository.mongodb.RestaurantRepository;
import com.example.mongoes.web.exception.DuplicateRestaurantException;
import com.example.mongoes.web.exception.RestaurantNotFoundException;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantESRepository restaurantESRepository;

    public RestaurantService(
            RestaurantRepository restaurantRepository,
            RestaurantESRepository restaurantESRepository) {
        this.restaurantRepository = restaurantRepository;
        this.restaurantESRepository = restaurantESRepository;
    }

    public Mono<SearchPage<Restaurant>> findAllRestaurants(int offset, int limit) {
        Sort sort = Sort.by(Sort.Direction.DESC, "restaurant_id");
        Pageable pageable = PageRequest.of(offset, limit, sort);
        return this.restaurantESRepository.findAll(pageable);
    }

    @Transactional
    public Mono<Restaurant> addGrade(GradesRequest gradesRequest, Long restaurantId) {
        return this.restaurantRepository
                .findByRestaurantId(restaurantId)
                .flatMap(
                        restaurant -> {
                            restaurant.getGrades().add(gradesRequest.toGrade());
                            return this.save(restaurant);
                        });
    }

    @Transactional
    public Mono<Restaurant> updateGrades(List<GradesRequest> gradesRequestList, Long restaurantId) {
        return this.restaurantRepository
                .findByRestaurantId(restaurantId)
                .flatMap(
                        restaurant -> {
                            restaurant.getGrades().clear();
                            restaurant
                                    .getGrades()
                                    .addAll(
                                            gradesRequestList.stream()
                                                    .map(GradesRequest::toGrade)
                                                    .toList());
                            return this.save(restaurant);
                        });
    }

    private Mono<Restaurant> save(Restaurant restaurant) {
        return this.restaurantRepository.save(restaurant);
    }

    public Mono<Restaurant> findByRestaurantName(String restaurantName) {
        return this.restaurantESRepository
                .findByName(restaurantName)
                .switchIfEmpty(
                        Mono.error(
                                new RestaurantNotFoundException(
                                        "Restaurant not found with name: " + restaurantName)));
    }

    public Mono<Restaurant> findByRestaurantId(Long restaurantId) {
        return this.restaurantESRepository
                .findByRestaurantId(restaurantId)
                .switchIfEmpty(
                        Mono.error(
                                new RestaurantNotFoundException(
                                        "Restaurant not found with id: " + restaurantId)));
    }

    public Mono<Long> totalCount() {
        return this.restaurantESRepository.count();
    }

    public Mono<Object> createRestaurant(RestaurantRequest restaurantRequest) {
        return restaurantRepository
                .findByName(restaurantRequest.name())
                .hasElement()
                .flatMap(
                        exists -> {
                            if (exists) {
                                String errorMessage =
                                        String.format(
                                                "Restaurant with name '%s' already exists",
                                                restaurantRequest.name());
                                return Mono.error(new DuplicateRestaurantException(errorMessage));
                            }
                            return restaurantRepository.save(restaurantRequest.toRestaurant());
                        });
    }
}
