package com.example.highrps.common;

import com.redis.testcontainers.RedisContainer;
import eu.rekawek.toxiproxy.Proxy;
import eu.rekawek.toxiproxy.ToxiproxyClient;
import java.time.Duration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.Network;
import org.testcontainers.grafana.LgtmStackContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.toxiproxy.ToxiproxyContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class ContainersConfig {

    @Bean
    @ServiceConnection
    LgtmStackContainer lgtmContainer() {
        return new LgtmStackContainer(DockerImageName.parse("grafana/otel-lgtm:0.30.0"))
                .withStartupTimeout(Duration.ofMinutes(2))
                .withReuse(true);
    }

    @Bean
    @ServiceConnection(name = "redis")
    RedisContainer redisContainer() {
        return new RedisContainer(DockerImageName.parse("redis").withTag("8.10.0-alpine")).withReuse(true);
    }

    @Bean(destroyMethod = "close")
    Network testNetwork() {
        return Network.newNetwork();
    }

    @Bean
    KafkaContainer kafkaContainer(Network testNetwork) {
        return new KafkaContainer(DockerImageName.parse("apache/kafka-native").withTag("4.3.1"))
                .withNetwork(testNetwork)
                .withNetworkAliases("kafka")
                .withReuse(true);
    }

    @Bean
    ToxiproxyContainer toxiproxyContainer(Network testNetwork) {
        return new ToxiproxyContainer("ghcr.io/shopify/toxiproxy:2.11.0")
                .withNetwork(testNetwork)
                .withReuse(true);
    }

    @Bean
    Proxy kafkaProxy(ToxiproxyContainer toxiproxyContainer, KafkaContainer kafkaContainer) throws Exception {
        ToxiproxyClient toxiproxyClient =
                new ToxiproxyClient(toxiproxyContainer.getHost(), toxiproxyContainer.getControlPort());
        return toxiproxyClient.createProxy(
                "kafka", "0.0.0.0:8666", kafkaContainer.getNetworkAliases().getFirst() + ":9092");
    }

    @Bean
    DynamicPropertyRegistrar dynamicPropertyRegistrar(
            RedisContainer redisContainer, ToxiproxyContainer toxiproxyContainer) {
        return registry -> {
            registry.add("spring.data.redis.host", redisContainer::getHost);
            registry.add("spring.data.redis.port", redisContainer::getFirstMappedPort);
            registry.add(
                    "spring.kafka.bootstrap-servers",
                    () -> toxiproxyContainer.getHost() + ":" + toxiproxyContainer.getMappedPort(8666));
        };
    }
}
