package com.example.grpc.spring;

import com.example.grpc.spring.config.ApplicationProperties;
import com.example.grpc.spring.proto.PostCommentServiceGrpc;
import com.example.grpc.spring.proto.PostServiceGrpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.grpc.client.ImportGrpcClients;

@SpringBootApplication
@EnableConfigurationProperties({ApplicationProperties.class})
@ImportGrpcClients(
        target = "blog",
        types = {
            PostServiceGrpc.PostServiceBlockingStub.class,
            PostCommentServiceGrpc.PostCommentServiceBlockingStub.class
        })
public class SpringGrpcApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringGrpcApplication.class, args);
    }
}
