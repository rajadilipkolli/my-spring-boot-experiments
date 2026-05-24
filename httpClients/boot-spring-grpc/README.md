# boot-spring-grpc

A Spring Boot module demonstrating gRPC server and REST API integration.

This sample exposes a minimal gRPC service defined by `src/main/proto/hello.proto` and a simple HTTP controller that delegates greeting logic to the same service layer.

## Features

- Spring Boot 4.0.6
- gRPC service using `grpc-spring-boot-starter`
- OpenAPI documentation with SpringDoc
- CORS configuration and global exception handling
- Protobuf build integration via `protobuf-maven-plugin`

## Running

```bash
./mvnw -pl httpClients/boot-spring-grpc spring-boot:run
```

## Endpoints

- HTTP GET `/api/hello?name=YourName`
- gRPC `HelloService/SayHello` on port `9090`
