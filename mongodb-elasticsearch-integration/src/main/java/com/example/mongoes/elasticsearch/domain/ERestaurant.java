package com.example.mongoes.elasticsearch.domain;

import com.example.mongoes.utils.ApplicationConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.annotations.Setting;
import org.springframework.data.geo.Point;

import java.util.List;

@Document(indexName = ApplicationConstants.RESTAURANT_COLLECTION)
@Setting(replicas = 1, shards = 2)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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

  @GeoPointField
  private Point location;

  @Field(store = true, type = FieldType.Text)
  private String street;

  @Field(store = true, type = FieldType.Integer)
  private Integer zipcode;

  @Field(type = FieldType.Nested)
  private List<ENotes> notes;
}
