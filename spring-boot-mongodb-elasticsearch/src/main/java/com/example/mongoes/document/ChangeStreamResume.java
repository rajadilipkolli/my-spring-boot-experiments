package com.example.mongoes.document;

import lombok.Getter;
import lombok.Setter;

import org.bson.BsonTimestamp;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Getter
@Setter
public class ChangeStreamResume {

    @Id private String id;

    private BsonTimestamp resumeTimestamp;
}
