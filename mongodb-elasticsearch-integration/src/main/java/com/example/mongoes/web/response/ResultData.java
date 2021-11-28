package com.example.mongoes.web.response;

import org.springframework.data.geo.Point;

import java.io.Serializable;

public record ResultData(String restaurantName, Point location,
                         Double distance) implements Serializable {

}
