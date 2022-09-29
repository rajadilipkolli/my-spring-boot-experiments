package com.example.mongoes.document;

import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@AllArgsConstructor
public class ChangeStreamResume {

    @Id private String resumeToken;
}
