package com.example.mongoes.mongodb.domain;

import com.example.mongoes.mongodb.customannotation.CascadeSaveList;
import lombok.Data;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "restaurant")
public class Restaurant {

  @Id private String id;

  @Field("rName")
  @Indexed(direction = IndexDirection.ASCENDING)
  @NotBlank
  @Size(min = 2)
  private String restaurantName;

  @NotBlank private String borough;

  @NotBlank private String cuisine;

  private String building;

  /**
   * {@code location} is stored in GeoJSON format.
   *
   * <pre>
   * <code>
   * {
   *   "type" : "Point",
   *   "coordinates" : [ x, y ]
   * }
   * </code>
   * </pre>
   */
  @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
  private GeoJsonPoint location;

  private String street;

  private String zipcode;

  @CascadeSaveList @DBRef private List<Notes> notes = new ArrayList<>();

  @Version private Long version;

  @CreatedBy private String createdBy;

  @CreatedDate
  @Field("cDate")
  private LocalDateTime createdDate;

  @LastModifiedBy private String lastModifiedBy;

  @LastModifiedDate
  @Field("lDate")
  private LocalDateTime lastModifiedDate;
}
