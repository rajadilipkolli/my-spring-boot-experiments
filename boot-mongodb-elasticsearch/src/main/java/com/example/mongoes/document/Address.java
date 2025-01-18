package com.example.mongoes.document;

import java.util.StringJoiner;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.Field;

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

    public String getBuilding() {
        return building;
    }

    public Address setBuilding(String building) {
        this.building = building;
        return this;
    }

    public Point getLocation() {
        return location;
    }

    public Address setLocation(Point location) {
        this.location = location;
        return this;
    }

    public String getStreet() {
        return street;
    }

    public Address setStreet(String street) {
        this.street = street;
        return this;
    }

    public Integer getZipcode() {
        return zipcode;
    }

    public Address setZipcode(Integer zipcode) {
        this.zipcode = zipcode;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Address.class.getSimpleName() + "[", "]")
                .add("building='" + building + "'")
                .add("location=" + location)
                .add("street='" + street + "'")
                .add("zipcode=" + zipcode)
                .toString();
    }
}
