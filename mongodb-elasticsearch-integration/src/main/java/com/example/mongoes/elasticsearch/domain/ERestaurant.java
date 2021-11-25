package com.example.mongoes.elasticsearch.domain;

import com.example.mongoes.utils.ApplicationConstants;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.core.geo.GeoJsonPoint;

@Document(indexName = ApplicationConstants.RESTAURANT_COLLECTION)
@Data
@Builder
public class ERestaurant {

  @Id private String id;

  @Field(store = true, type = FieldType.Text)
  private String restaurantName;

  @Field(store = true, type = FieldType.Text)
  private String borough;

  @Field(store = true, type = FieldType.Text)
  private String cuisine;

  @Field(store = true, type = FieldType.Text)
  private String building;

  @Field(type = FieldType.Percolator)
  private GeoJsonPoint location;

  @Field(store = true, type = FieldType.Text)
  private String street;

  @Field(store = true, type = FieldType.Integer)
  private String zipcode;
}
