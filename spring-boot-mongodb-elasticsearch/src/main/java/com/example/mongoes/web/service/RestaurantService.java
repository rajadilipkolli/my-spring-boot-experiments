package com.example.mongoes.web.service;

import com.example.mongoes.document.Address;
import com.example.mongoes.document.ChangeStreamResume;
import com.example.mongoes.document.Grades;
import com.example.mongoes.document.Restaurant;
import com.example.mongoes.elasticsearch.repository.RestaurantESRepository;
import com.example.mongoes.mongodb.repository.ChangeStreamResumeRepository;
import com.example.mongoes.mongodb.repository.RestaurantRepository;
import com.example.mongoes.response.ResultData;
import com.example.mongoes.utils.AppConstants;
import com.example.mongoes.utils.DateUtility;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.OperationType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonTimestamp;
import org.bson.Document;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ChangeStreamOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantESRepository restaurantESRepository;
    private final ChangeStreamResumeRepository changeStreamResumeRepository;

    private final ReactiveMongoTemplate reactiveMongoTemplate;

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
                                    Point geoJsonPoint = new Point(obj.get(0), obj.get(1));
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

    @Transactional
    public Mono<Restaurant> addGrade(Grades grade, Long restaurantId) {
        return this.restaurantRepository
                .findByRestaurantId(restaurantId)
                .flatMap(
                        restaurant -> {
                            restaurant.getGrades().add(grade);
                            return this.save(restaurant);
                        });
    }

    private Mono<Restaurant> save(Restaurant restaurant) {
        return this.restaurantRepository.save(restaurant);
    }

    public Mono<Restaurant> findByRestaurantName(String restaurantName) {
        return this.restaurantESRepository.findByName(restaurantName);
    }

    public Mono<Restaurant> findByRestaurantId(Long restaurantId) {
        return this.restaurantESRepository.findByRestaurantId(restaurantId);
    }

    public Mono<Long> totalCount() {
        return this.restaurantESRepository.count();
    }

    public Flux<ChangeStreamEvent<Restaurant>> changeStreamProcessor() {
        return reactiveMongoTemplate
                .changeStream(Restaurant.class)
                .watchCollection(AppConstants.RESTAURANT_COLLECTION)
                .resumeAt(getChangeStreamOption())
                .listen()
                .delayElements(Duration.ofMillis(5))
                .doOnNext(
                        restaurantChangeStreamEvent -> {
                            log.info(
                                    "processed at {} ", restaurantChangeStreamEvent.getTimestamp());
                            Restaurant restaurant = restaurantChangeStreamEvent.getBody();
                            if (restaurant != null) {
                                if (restaurantChangeStreamEvent.getOperationType()
                                        == OperationType.DELETE) {
                                    this.restaurantESRepository
                                            .delete(restaurant)
                                            .log()
                                            .subscribe();
                                } else if (restaurantChangeStreamEvent.getOperationType()
                                                == OperationType.REPLACE
                                        || restaurantChangeStreamEvent.getOperationType()
                                                == OperationType.INSERT) {
                                    this.restaurantESRepository.save(restaurant).log().subscribe();
                                }
                            } else {
                                ChangeStreamDocument<Document> eventRaw =
                                        restaurantChangeStreamEvent.getRaw();
                                if (Objects.requireNonNull(eventRaw).getOperationType()
                                        == OperationType.DELETE) {
                                    var objectId =
                                            eventRaw.getDocumentKey()
                                                    .get("_id")
                                                    .asObjectId()
                                                    .getValue()
                                                    .toString();
                                    this.restaurantESRepository.deleteById(objectId).subscribe();
                                }
                            }

                            this.changeStreamResumeRepository
                                    .update(restaurantChangeStreamEvent.getBsonTimestamp())
                                    .log()
                                    .subscribe();
                        })
                .log("completed processing");
    }

    private ChangeStreamOptions getChangeStreamOption() {
        List<ChangeStreamResume> resumeTokenList = getResumeToken();
        ChangeStreamOptions changeStreamOption;
        if (resumeTokenList.isEmpty()) {
            // Scenario where MongoDb is started freshly hence resumeToken is empty
            changeStreamOption =
                    ChangeStreamOptions.builder()
                            .resumeAt(new BsonTimestamp(Instant.now().getEpochSecond()))
                            .build();
        } else {
            changeStreamOption =
                    ChangeStreamOptions.builder()
                            .resumeAt(resumeTokenList.get(0).getResumeTimestamp())
                            .build();
        }
        return changeStreamOption;
    }

    private List<ChangeStreamResume> getResumeToken() {
        return this.changeStreamResumeRepository.findAll().toStream().toList();
    }

    public Flux<Restaurant> findAllRestaurants() {
        Sort sort = Sort.by(Sort.Direction.DESC, "restaurantId");
        return this.restaurantESRepository.findAll(sort);
    }

    public Mono<Restaurant> createRestaurant(Restaurant restaurant) {
        return save(restaurant);
    }

    public Flux<ResultData> searchRestaurantsWithInRange(
            Double lat, Double lon, Double distance, String unit) {
        GeoPoint location = new GeoPoint(lat, lon);
        return this.restaurantESRepository
                .searchWithin(location, distance, unit)
                .flatMap(
                        eRestaurantSearchHit -> {
                            Double dist = (Double) eRestaurantSearchHit.getSortValues().get(0);
                            Restaurant eRestaurant = eRestaurantSearchHit.getContent();
                            return Mono.just(
                                    new ResultData(
                                            eRestaurant.getName(),
                                            eRestaurant.getAddress().getLocation(),
                                            dist));
                        });
    }
}
