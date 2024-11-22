package com.example.multitenancy;

import com.example.multitenancy.common.ContainersConfiguration;
import com.example.multitenancy.utils.AppConstants;
import org.springframework.boot.SpringApplication;

class MultiDataSourceMultiTenancyApplicationTest {

    public static void main(String[] args) {
        SpringApplication.from(MultiDataSourceMultiTenancyApplication::main)
                .with(ContainersConfiguration.class)
                .withAdditionalProfiles(AppConstants.PROFILE_TEST)
                .run(args);
    }
}
