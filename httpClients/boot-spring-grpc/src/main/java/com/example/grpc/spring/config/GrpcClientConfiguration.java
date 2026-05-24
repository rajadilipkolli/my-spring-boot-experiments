package com.example.grpc.spring.config;

import com.example.grpc.spring.proto.PostCommentServiceGrpc;
import com.example.grpc.spring.proto.PostServiceGrpc;
import io.grpc.ManagedChannel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration(proxyBeanMethods = false)
public class GrpcClientConfiguration {

    @Bean
    public ManagedChannel localGrpcChannel(GrpcChannelFactory channelFactory) {
        return channelFactory.createChannel("local");
    }

    @Bean
    public PostServiceGrpc.PostServiceBlockingStub postServiceBlockingStub(
            ManagedChannel localGrpcChannel) {
        return PostServiceGrpc.newBlockingStub(localGrpcChannel);
    }

    @Bean
    public PostCommentServiceGrpc.PostCommentServiceBlockingStub postCommentServiceBlockingStub(
            ManagedChannel localGrpcChannel) {
        return PostCommentServiceGrpc.newBlockingStub(localGrpcChannel);
    }
}
