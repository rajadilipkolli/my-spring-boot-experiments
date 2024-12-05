package com.example.mongoes.document;

import org.bson.BsonTimestamp;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class ChangeStreamResume {

    @Id private String id;

    private BsonTimestamp resumeTimestamp;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BsonTimestamp getResumeTimestamp() {
        return resumeTimestamp;
    }

    public void setResumeTimestamp(BsonTimestamp resumeTimestamp) {
        this.resumeTimestamp = resumeTimestamp;
    }
}
