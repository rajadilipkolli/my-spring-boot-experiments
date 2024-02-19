package com.example.mongoes;

import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestApplication {

    @ServiceConnection
    @Bean
    @RestartScope
    ElasticsearchContainer elasticsearchContainer() {
        return new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:8.12.1")
                .withEnv(Map.of("xpack.security.enabled", "false"));
    }

    @ServiceConnection
    @Bean
    @RestartScope
    MongoDBContainer mongoDBContainer() {
        return new MongoDBContainer(DockerImageName.parse("mongo").withTag("7.0.5")).withSharding();
    }

    public static void main(String[] args) {
        SpringApplication.from(Application::main).with(TestApplication.class).run(args);
    }
}
