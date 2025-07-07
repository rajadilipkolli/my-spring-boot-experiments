package com.example.featuretoggle.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.spring.boot.actuate.autoconfigure.TogglzProperties;

@Component
public class Initializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(Initializer.class);

    private final StateRepository jdbcStateRepository;
    private final TogglzProperties togglzProperties;

    public Initializer(StateRepository jdbcStateRepository, TogglzProperties togglzProperties) {
        this.jdbcStateRepository = jdbcStateRepository;
        this.togglzProperties = togglzProperties;
    }

    @Override
    public void run(String... args) {
        log.info("Running Initializer.....");

        // Manually inserting all values from properties to db
        togglzProperties
                .getFeatures()
                .forEach(
                        (featureName, featureSpec) -> {
                            FeatureState featureState =
                                    new FeatureState(() -> featureName, featureSpec.isEnabled());
                            featureState.setStrategyId(featureSpec.getStrategy());
                            featureSpec.getParam().forEach(featureState::setParameter);
                            jdbcStateRepository.setFeatureState(featureState);
                        });
    }
}
