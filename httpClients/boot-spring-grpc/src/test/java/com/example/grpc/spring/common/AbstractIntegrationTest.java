package com.example.grpc.spring.common;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.example.grpc.spring.proto.PostCommentServiceGrpc;
import com.example.grpc.spring.proto.PostServiceGrpc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.grpc.test.autoconfigure.AutoConfigureTestGrpcTransport;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = "spring.grpc.server.port=0")
@AutoConfigureMockMvc
@AutoConfigureTestGrpcTransport
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @Autowired protected MockMvc mockMvc;

    @Autowired protected JsonMapper jsonMapper;

    @Autowired protected PostServiceGrpc.PostServiceBlockingStub postServiceBlockingStub;

    @Autowired
    protected PostCommentServiceGrpc.PostCommentServiceBlockingStub postCommentServiceBlockingStub;
}
