package com.example.mongoes.web.service;

import com.example.mongoes.document.Address;
import com.example.mongoes.document.ChangeStreamResume;
import com.example.mongoes.document.Grades;
import com.example.mongoes.document.Restaurant;
import com.example.mongoes.elasticsearch.repository.RestaurantESRepository;
import com.example.mongoes.mongodb.repository.ChangeStreamResumeRepository;
import com.example.mongoes.mongodb.repository.RestaurantRepository;
import com.example.mongoes.utils.AppConstants;
import com.example.mongoes.utils.DateUtility;
import com.example.mongoes.web.model.RestaurantRequest;
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
import org.bson.BsonTimestamp;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ChangeStreamOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class RestaurantService {

    private static final Logger log = LoggerFactory.getLogger(RestaurantService.class);
    private final RestaurantRepository restaurantRepository;
    private final RestaurantESRepository restaurantESRepository;
    private final ChangeStreamResumeRepository changeStreamResumeRepository;
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public RestaurantService(
            RestaurantRepository restaurantRepository,
            RestaurantESRepository restaurantESRepository,
            ChangeStreamResumeRepository changeStreamResumeRepository,
            ReactiveMongoTemplate reactiveMongoTemplate) {
        this.restaurantRepository = restaurantRepository;
        this.restaurantESRepository = restaurantESRepository;
        this.changeStreamResumeRepository = changeStreamResumeRepository;
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    public Flux<Restaurant> loadData() throws IOException {
        Resource input = new ClassPathResource("restaurants.json");
        Path path = input.getFile().toPath();
        var restaurantArray = Files.readAllLines(path);
        return this.saveAll(restaurantArray);
    }

    private Flux<Restaurant> saveAll(List<String> restaurantStringList) {
        return Flux.fromIterable(restaurantStringList)
                .map(Document::parse)
                .map(this::documentToRestaurant)
                .flatMap(restaurantRepository::save);
    }

    private Restaurant documentToRestaurant(Document document) {
        Restaurant restaurant = new Restaurant();
        restaurant.setRestaurantId(Long.valueOf(document.get("restaurant_id", String.class)));
        restaurant.setName(document.get("name", String.class));
        restaurant.setCuisine(document.get("cuisine", String.class));
        restaurant.setBorough(document.get("borough", String.class));

        Address address = new Address();
        Document addressDoc = document.get("address", Document.class);
        address.setBuilding(addressDoc.get("building", String.class));
        address.setStreet(addressDoc.get("street", String.class));
        address.setZipcode(Integer.valueOf(addressDoc.get("zipcode", String.class)));
        List<Double> coord = addressDoc.getList("coord", Double.class);
        Point geoJsonPoint = new Point(coord.get(0), coord.get(1));
        address.setLocation(geoJsonPoint);
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
        ChangeStreamOptions.ChangeStreamOptionsBuilder changeStreamOptionsBuilder;
        if (resumeTokenList.isEmpty()) {
            // Scenario where MongoDb is started freshly hence resumeToken is empty
            changeStreamOptionsBuilder =
                    ChangeStreamOptions.builder()
                            .resumeAt(new BsonTimestamp(Instant.now().getEpochSecond()));
        } else {
            changeStreamOptionsBuilder =
                    ChangeStreamOptions.builder()
                            .resumeAt(resumeTokenList.getFirst().getResumeTimestamp());
        }
        changeStreamOptionsBuilder.returnFullDocumentOnUpdate();
        return changeStreamOptionsBuilder.build();
    }

    private List<ChangeStreamResume> getResumeToken() {
        return this.changeStreamResumeRepository.findAll().toStream().toList();
    }

    public Mono<SearchPage<Restaurant>> findAllRestaurants(int offset, int limit) {
        Sort sort = Sort.by(Sort.Direction.DESC, "restaurant_id");
        Pageable pageable = PageRequest.of(offset, limit, sort);
        return this.restaurantESRepository.findAll(pageable);
    }

    public Mono<Restaurant> createRestaurant(RestaurantRequest restaurantRequest) {

        return save(restaurantRequest.toRestaurant());
    }

    public Mono<Void> deleteAll() {
        return this.restaurantRepository.deleteAll();
    }
}
