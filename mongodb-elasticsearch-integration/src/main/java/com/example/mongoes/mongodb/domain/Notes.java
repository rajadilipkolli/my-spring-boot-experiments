package com.example.mongoes.mongodb.domain;

import com.example.mongoes.utils.ApplicationConstants;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@Document(collection = ApplicationConstants.NOTE_COLLECTION)
public class Notes {

  @Id private String id;

  private String note;

  private LocalDate date;

  private Integer score;

  public Notes(String notes, LocalDate localDate, int score) {
    this.note = notes;
    this.date = localDate;
    this.score = score;
  }
}
