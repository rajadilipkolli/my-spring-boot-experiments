# boot-spring-grpc

A Spring Boot module demonstrating gRPC server and REST API integration.

This sample exposes gRPC services defined by `src/main/proto/blog.proto` (PostService and PostCommentService) and REST controllers that delegate to gRPC client services for managing posts and comments.

## Features

- Spring Boot 4+
- gRPC service using `grpc-spring-boot-starter`
- OpenAPI documentation with SpringDoc
- CORS configuration and global exception handling
- Protobuf build integration via `protobuf-maven-plugin`

## Running

```bash
./mvnw -pl httpClients/boot-spring-grpc spring-boot:run
```

## Endpoints

### REST API (port 8080)

**Posts:**
- `POST /api/posts` - Create a post
- `GET /api/posts` - List all posts
- `GET /api/posts/{id}` - Get post by ID
- `PUT /api/posts/{id}` - Update post
- `DELETE /api/posts/{id}` - Delete post

**Comments:**
- `POST /api/posts/{postId}/comments` - Add comment to post
- `GET /api/posts/{postId}/comments` - List comments for post
- `GET /api/posts/{postId}/comments/{id}` - Get comment by ID
- `PUT /api/posts/{postId}/comments/{id}` - Update comment
- `DELETE /api/posts/{postId}/comments/{id}` - Delete comment

### gRPC Services (port 9090)

- `PostService` - CRUD operations for posts
- `PostCommentService` - CRUD operations for post comments