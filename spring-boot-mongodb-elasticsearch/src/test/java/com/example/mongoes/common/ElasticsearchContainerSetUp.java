package com.example.mongoes.common;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.lifecycle.Startables;

import java.util.Map;

public class ElasticsearchContainerSetUp {

    protected static final ElasticsearchContainer ELASTICSEARCH_CONTAINER =
            new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:8.5.2")
                    .withEnv(
                            Map.of(
                                    "discovery.type",
                                    "single-node",
                                    "xpack.security.enabled",
                                    "false"))
                    .withReuse(true);

    static {
        Startables.deepStart(ELASTICSEARCH_CONTAINER).join();
    }

    @DynamicPropertySource
    static void setApplicationProperties(DynamicPropertyRegistry propertyRegistry) {
        propertyRegistry.add(
                "spring.elasticsearch.uris", ELASTICSEARCH_CONTAINER::getHttpHostAddress);
    }
}
