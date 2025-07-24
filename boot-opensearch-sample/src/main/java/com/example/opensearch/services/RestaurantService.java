package com.example.opensearch.services;

import com.example.opensearch.entities.Restaurant;
import com.example.opensearch.model.response.PagedResult;
import com.example.opensearch.repositories.RestaurantRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public RestaurantService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    public PagedResult<Restaurant> findAllRestaurants(int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        // create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<Restaurant> restaurantsPage = restaurantRepository.findAll(pageable);

        return new PagedResult<>(restaurantsPage);
    }

    public Optional<Restaurant> findRestaurantById(String id) {
        return restaurantRepository.findById(id);
    }

    public Restaurant saveRestaurant(Restaurant restaurant) {
        return restaurantRepository.save(restaurant);
    }

    public void deleteRestaurantById(String id) {
        restaurantRepository.deleteById(id);
    }
}
