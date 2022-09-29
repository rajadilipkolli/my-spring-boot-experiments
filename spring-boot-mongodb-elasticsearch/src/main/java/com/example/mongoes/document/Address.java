package com.example.mongoes.document;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Address {

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
    @JsonDeserialize(as = Point.class)
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    @Field("coord")
    private GeoJsonPoint location;

    private String street;

    private Integer zipcode;
}
