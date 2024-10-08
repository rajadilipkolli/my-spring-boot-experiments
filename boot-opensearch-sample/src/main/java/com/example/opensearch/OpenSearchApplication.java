package com.example.opensearch;

import com.example.opensearch.config.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(exclude = {ElasticsearchDataAutoConfiguration.class})
@EnableConfigurationProperties({ApplicationProperties.class})
public class OpenSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenSearchApplication.class, args);
    }
}
