package com.example.mongoes.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.geo.Point;
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
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    @GeoPointField
    @org.springframework.data.elasticsearch.annotations.Field("coord")
    @Field("coord")
    private Point location;

    private String street;

    private Integer zipcode;
}
