package com.example.grpc.spring.services;

import com.example.grpc.spring.proto.HelloRequest;
import com.example.grpc.spring.proto.HelloResponse;
import com.example.grpc.spring.proto.HelloServiceGrpc;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;

@GRpcService
public class HelloGrpcService extends HelloServiceGrpc.HelloServiceImplBase {

    private final HelloService helloService;

    public HelloGrpcService(HelloService helloService) {
        this.helloService = helloService;
    }

    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
        HelloResponse response = HelloResponse.newBuilder()
                .setMessage(helloService.buildGreeting(request.getName()))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
