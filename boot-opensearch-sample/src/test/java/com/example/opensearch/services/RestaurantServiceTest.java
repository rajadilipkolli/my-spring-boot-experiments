package com.example.opensearch.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.willDoNothing;

import com.example.opensearch.entities.Restaurant;
import com.example.opensearch.model.response.PagedResult;
import com.example.opensearch.repositories.RestaurantRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private RestaurantService restaurantService;

    @Test
    void findAllRestaurants() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        Page<Restaurant> restaurantPage = new PageImpl<>(List.of(getRestaurant()));
        given(restaurantRepository.findAll(pageable)).willReturn(restaurantPage);

        // when
        PagedResult<Restaurant> pagedResult = restaurantService.findAllRestaurants(0, 10, "id", "asc");

        // then
        assertThat(pagedResult).isNotNull();
        assertThat(pagedResult.data()).isNotEmpty().hasSize(1);
        assertThat(pagedResult.hasNext()).isFalse();
        assertThat(pagedResult.pageNumber()).isOne();
        assertThat(pagedResult.totalPages()).isOne();
        assertThat(pagedResult.isFirst()).isTrue();
        assertThat(pagedResult.isLast()).isTrue();
        assertThat(pagedResult.hasPrevious()).isFalse();
        assertThat(pagedResult.totalElements()).isOne();
    }

    @Test
    void findRestaurantById() {
        // given
        given(restaurantRepository.findById("1")).willReturn(Optional.of(getRestaurant()));
        // when
        Optional<Restaurant> optionalRestaurant = restaurantService.findRestaurantById("1");
        // then
        assertThat(optionalRestaurant).isPresent();
        Restaurant restaurant = optionalRestaurant.get();
        assertThat(restaurant.getId()).isEqualTo("1");
        assertThat(restaurant.getName()).isEqualTo("junitTest");
    }

    @Test
    void saveRestaurant() {
        // given
        given(restaurantRepository.save(getRestaurant())).willReturn(getRestaurant());
        // when
        Restaurant persistedRestaurant = restaurantService.saveRestaurant(getRestaurant());
        // then
        assertThat(persistedRestaurant).isNotNull();
        assertThat(persistedRestaurant.getId()).isEqualTo("1");
        assertThat(persistedRestaurant.getName()).isEqualTo("junitTest");
    }

    @Test
    void deleteRestaurantById() {
        // given
        willDoNothing().given(restaurantRepository).deleteById("1");
        // when
        restaurantService.deleteRestaurantById("1");
        // then
        verify(restaurantRepository, times(1)).deleteById("1");
    }

    private Restaurant getRestaurant() {
        Restaurant restaurant = new Restaurant();
        restaurant.setId("1");
        restaurant.setName("junitTest");
        return restaurant;
    }
}
