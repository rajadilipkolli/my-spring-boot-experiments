package com.example.mongoes.elasticsearch.domain;

import com.example.mongoes.utils.ApplicationConstants;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.time.LocalDate;

@Document(indexName = ApplicationConstants.NOTE_COLLECTION)
@Setting(replicas = 1, shards = 2)
@Data
@Builder
public class ENotes {

  @Id private String id;

  @Field(store = true, type = FieldType.Text)
  private String note;

  @Field(store = true, type = FieldType.Date)
  private LocalDate date;

  @Field(store = true, type = FieldType.Integer)
  private Integer score;
}
