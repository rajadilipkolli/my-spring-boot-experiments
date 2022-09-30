package com.example.mongoes.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class ChangeStreamResume {

    @Id private String id;

    private String resumeToken;
}
