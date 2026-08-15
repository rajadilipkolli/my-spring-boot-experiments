package com.example.highrps.common;

import com.redis.testcontainers.RedisContainer;
import eu.rekawek.toxiproxy.Proxy;
import eu.rekawek.toxiproxy.ToxiproxyClient;
import java.time.Duration;
import java.util.UUID;
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
        return new LgtmStackContainer(DockerImageName.parse("grafana/otel-lgtm:0.30.1"))
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
    KafkaContainer kafkaContainer(Network testNetwork, ToxiproxyContainer toxiproxyContainer) {
        return new KafkaContainer(DockerImageName.parse("apache/kafka-native").withTag("4.3.1")) {
            @Override
            public String getBootstrapServers() {
                return toxiproxyContainer.getHost() + ":" + toxiproxyContainer.getMappedPort(8666);
            }
        }.withNetwork(testNetwork)
                .withNetworkAliases("kafka")
                .dependsOn(toxiproxyContainer)
                .withReuse(true);
    }

    @Bean
    ToxiproxyContainer toxiproxyContainer(Network testNetwork) {
        return new ToxiproxyContainer("ghcr.io/shopify/toxiproxy:2.11.0")
                .withNetwork(testNetwork)
                .withReuse(true);
    }

    @Bean
    Proxy kafkaProxy(ToxiproxyContainer toxiproxyContainer) throws Exception {
        ToxiproxyClient toxiproxyClient =
                new ToxiproxyClient(toxiproxyContainer.getHost(), toxiproxyContainer.getControlPort());
        try {
            return toxiproxyClient.getProxy("kafka");
        } catch (Exception e) {
            return toxiproxyClient.createProxy("kafka", "0.0.0.0:8666", "kafka:9092");
        }
    }

    @Bean
    DynamicPropertyRegistrar dynamicPropertyRegistrar(RedisContainer redisContainer, KafkaContainer kafkaContainer) {
        String stateDir = System.getProperty("java.io.tmpdir") + "/kafka-streams-test-state-" + UUID.randomUUID();
        return registry -> {
            registry.add("spring.data.redis.host", redisContainer::getHost);
            registry.add("spring.data.redis.port", redisContainer::getFirstMappedPort);
            registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
            registry.add("spring.kafka.streams.state-dir", () -> stateDir);
        };
    }
}
