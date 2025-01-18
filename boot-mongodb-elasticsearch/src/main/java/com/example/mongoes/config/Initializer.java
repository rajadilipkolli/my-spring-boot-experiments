package com.example.mongoes.config;

import com.example.mongoes.utils.AppConstants;
import com.example.mongoes.web.service.RestaurantService;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile(AppConstants.PROFILE_NOT_TEST)
public class Initializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(Initializer.class);
    private final RestaurantService restaurantService;

    public Initializer(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @Override
    public void run(String... args) throws IOException {
        log.info("Running Initializer.....");
        restaurantService
                .deleteAll()
                .thenMany(restaurantService.loadData())
                .log()
                .subscribe(null, null, () -> log.info("done initialization..."));
    }
}
