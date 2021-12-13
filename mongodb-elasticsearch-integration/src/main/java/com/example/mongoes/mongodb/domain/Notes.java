package com.example.mongoes.mongodb.domain;

import com.example.mongoes.utils.ApplicationConstants;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Setter
@Getter
@ToString
@NoArgsConstructor
@Document(collection = ApplicationConstants.NOTE_COLLECTION)
public class Notes {

  @Id private String id;

  private String note;

  private LocalDate date = LocalDate.now();

  private Integer score;

  public Notes(String note, LocalDate localDate, int score) {
    this.note = note;
    this.date = localDate;
    this.score = score;
  }

  public Notes(String note, int score) {
    this.note = note;
    this.score = score;
  }
}
