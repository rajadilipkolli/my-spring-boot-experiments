package com.example.mongoes.config;

import com.example.mongoes.document.Address;
import com.example.mongoes.document.Grades;
import com.example.mongoes.document.Restaurant;
import com.example.mongoes.repository.mongodb.RestaurantRepository;
import com.example.mongoes.utils.AppConstants;
import com.example.mongoes.utils.DateUtility;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@Profile(AppConstants.PROFILE_NOT_TEST)
public class Initializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(Initializer.class);
    private final RestaurantRepository restaurantRepository;

    public Initializer(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @Override
    public void run(String... args) throws IOException {
        log.info("Running Initializer.....");
        restaurantRepository
                .deleteAll()
                .thenMany(this.loadData())
                .log()
                .subscribe(
                        null,
                        error -> log.error("Error during initialization: ", error),
                        () -> log.info("Done initialization."));
    }

    private Flux<Restaurant> loadData() {
        return DataBufferUtils.join(
                        DataBufferUtils.read(
                                new ClassPathResource("restaurants.json"),
                                new DefaultDataBufferFactory(),
                                4096))
                .map(
                        dataBuffer -> {
                            byte[] bytes = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(bytes);
                            DataBufferUtils.release(dataBuffer);
                            return new String(bytes, StandardCharsets.UTF_8);
                        })
                .flatMapMany(
                        fileContent -> {
                            List<String> restaurantArray = Arrays.asList(fileContent.split("\n"));
                            return this.saveAll(restaurantArray);
                        });
    }

    private Flux<Restaurant> saveAll(List<String> restaurantStringList) {
        return Flux.fromIterable(restaurantStringList)
                .map(Document::parse)
                .map(this::documentToRestaurant)
                .flatMap(restaurantRepository::save, 10);
    }

    private Restaurant documentToRestaurant(Document document) {
        Restaurant restaurant = new Restaurant();
        String restaurantId = document.get("restaurant_id", String.class);
        if (restaurantId == null || restaurantId.isBlank()) {
            throw new IllegalArgumentException("Restaurant ID is required");
        }
        restaurant.setRestaurantId(Long.valueOf(restaurantId));
        restaurant.setName(document.get("name", String.class));
        restaurant.setCuisine(document.get("cuisine", String.class));
        restaurant.setBorough(document.get("borough", String.class));

        Address address = new Address();
        Document addressDoc = document.get("address", Document.class);
        address.setBuilding(addressDoc.get("building", String.class));
        address.setStreet(addressDoc.get("street", String.class));
        address.setZipcode(Integer.valueOf(addressDoc.get("zipcode", String.class)));
        List<Double> coord = addressDoc.getList("coord", Double.class);
        if (coord.size() == 2) {
            Point geoJsonPoint = new Point(coord.getFirst(), coord.getLast());
            address.setLocation(geoJsonPoint);
        } else {
            // Handle the error case appropriately
            log.warn("Invalid coordinates for restaurant ID: {}", restaurant.getRestaurantId());
        }

        restaurant.setAddress(address);

        List<Grades> gradesList = getGradesList(document.getList("grades", Document.class));
        restaurant.setGrades(gradesList);

        return restaurant;
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
}
