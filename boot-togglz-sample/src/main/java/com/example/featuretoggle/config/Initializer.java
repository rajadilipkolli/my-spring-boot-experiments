package com.example.featuretoggle.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.spring.boot.actuate.autoconfigure.TogglzProperties;

@Component
@Slf4j
@RequiredArgsConstructor
public class Initializer implements CommandLineRunner {

    private final StateRepository jdbcStateRepository;
    private final TogglzProperties togglzProperties;

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
