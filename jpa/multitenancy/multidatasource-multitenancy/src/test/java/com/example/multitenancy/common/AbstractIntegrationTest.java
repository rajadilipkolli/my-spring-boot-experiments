package com.example.multitenancy.common;

import static com.example.multitenancy.utils.AppConstants.PROFILE_TEST;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.example.multitenancy.config.multitenant.TenantIdentifierResolver;
import com.example.multitenancy.primary.repositories.PrimaryCustomerRepository;
import com.example.multitenancy.secondary.repositories.SecondaryCustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@ActiveProfiles({PROFILE_TEST})
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = ContainersConfiguration.class)
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected PrimaryCustomerRepository primaryCustomerRepository;

    @Autowired
    protected SecondaryCustomerRepository secondaryCustomerRepository;

    @Autowired
    protected TenantIdentifierResolver tenantIdentifierResolver;
}
