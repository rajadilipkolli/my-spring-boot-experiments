package com.example.mongoes.web.service;

import com.example.mongoes.mongodb.document.Address;
import com.example.mongoes.mongodb.document.Grades;
import com.example.mongoes.mongodb.document.Restaurant;
import com.example.mongoes.mongodb.repository.RestaurantRepository;
import com.example.mongoes.utils.DateUtility;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public Flux<Restaurant> loadData() throws IOException {
        Resource input = new ClassPathResource("restaurants.json");
        Path path = input.getFile().toPath();
        var restaurantArray = Files.readAllLines(path);
        return this.saveAll(restaurantArray);
    }

    private Flux<Restaurant> saveAll(List<String> restaurantStringList) {
        List<Restaurant> restaurantList =
                restaurantStringList.stream()
                        .map(Document::parse)
                        .map(
                                document -> {
                                    Restaurant restaurant = new Restaurant();
                                    restaurant.setRestaurantId(
                                            Long.valueOf(
                                                    document.get("restaurant_id", String.class)));
                                    restaurant.setName(document.get("name", String.class));
                                    restaurant.setCuisine(document.get("cuisine", String.class));
                                    restaurant.setBorough(document.get("borough", String.class));
                                    Address address = new Address();
                                    Document addressDoc = (Document) document.get("address");
                                    address.setBuilding(addressDoc.get("building", String.class));
                                    address.setStreet(addressDoc.get("street", String.class));
                                    address.setZipcode(
                                            Integer.valueOf(
                                                    addressDoc.get("zipcode", String.class)));
                                    List<Double> obj = addressDoc.getList("coord", Double.class);
                                    GeoJsonPoint geoJsonPoint =
                                            new GeoJsonPoint(obj.get(0), obj.get(1));
                                    address.setLocation(geoJsonPoint);
                                    restaurant.setAddress(address);
                                    List<Grades> gradesList =
                                            getGradesList(
                                                    document.getList("grades", Document.class));
                                    restaurant.setGrades(gradesList);

                                    return restaurant;
                                })
                        .toList();
        return restaurantRepository.saveAll(restaurantList);
    }

    private List<Grades> getGradesList(List<Document> gradeDocumentList) {
        return gradeDocumentList.stream()
                .map(
                        gradeDoc -> {
                            Grades grades = new Grades();
                            grades.setGrade(gradeDoc.get("grade", String.class));
                            grades.setScore(gradeDoc.get("score", Integer.class));
                            grades.setDate(
                                    DateUtility.convertToLocalDateViaInstant(
                                            gradeDoc.get("date", Date.class)));
                            return grades;
                        })
                .toList();
    }

    public Mono<Restaurant> addGrade(Grades grade, Long restaurantId) {
        return this.find(restaurantId)
                .flatMap(
                        restaurant -> {
                            restaurant.getGrades().add(grade);
                            return this.save(restaurant);
                        });
    }

    private Mono<Restaurant> save(Restaurant restaurant) {
        return this.restaurantRepository.save(restaurant);
    }

    private Mono<Restaurant> find(Long restaurantId) {
        return this.restaurantRepository.findByRestaurantId(restaurantId);
    }

    public Flux<Restaurant> findAll() {
        return this.restaurantRepository.findAll();
    }

    public Mono<Restaurant> findByRestaurantName(String restaurantName) {
        return this.restaurantRepository.findByName(restaurantName);
    }

    public Mono<Restaurant> findByRestaurantId(Long restaurantId) {
        return this.restaurantRepository.findByRestaurantId(restaurantId);
    }

    public Mono<Void> deleteAll() {
        return this.restaurantRepository.deleteAll().log("Deleted All Restaurants");
    }

    public Mono<Long> totalCount() {
        return this.restaurantRepository.count();
    }
}
