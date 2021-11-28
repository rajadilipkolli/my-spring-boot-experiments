package com.example.mongoes.web.response;

import org.springframework.data.geo.Point;

public record ResultData(String restaurantName, Point location,
                         Double distance) {

}
