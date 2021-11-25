package com.example.mongoes.bootstrap;

import com.example.mongoes.mongodb.domain.Notes;
import com.example.mongoes.mongodb.domain.Restaurant;
import com.example.mongoes.mongodb.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class MongoDataInitializer {

  private final RestaurantRepository restaurantRepository;

  @EventListener(ApplicationReadyEvent.class)
  void loadInitialData() {
    Notes notes = new Notes(RandomStringUtils.randomAlphabetic(5), LocalDate.now(), 20);
    Notes notes1 = new Notes("Notes2", LocalDate.now().minusDays(15), 10);
    Notes notes2 =
        new Notes(RandomStringUtils.randomAlphabetic(5), LocalDate.now().minusMonths(5), 50);
    Restaurant restaurant = new Restaurant();
    restaurant.setBuilding(RandomStringUtils.randomAlphabetic(5));
    restaurant.setStreet(RandomStringUtils.randomAlphabetic(5));
    restaurant.setZipcode(RandomStringUtils.randomNumeric(4));
    restaurant.setLocation(new GeoJsonPoint(-73.9387768, 40.8509032));
    restaurant.setRestaurantName(RandomStringUtils.randomAlphabetic(5));
    restaurant.setBorough(RandomStringUtils.randomAlphabetic(5));
    restaurant.setCuisine(RandomStringUtils.randomAlphabetic(5));
    restaurant.setNotes(List.of(notes, notes1, notes2));

    this.restaurantRepository.deleteAll();

    Restaurant savedRestaurant = this.restaurantRepository.save(restaurant);
    log.info("Saved Restaurant :{}", savedRestaurant);
  }
}
