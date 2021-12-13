package com.example.mongoes.web.exception;

import org.springframework.http.HttpStatus;

public class RestaurantNotFoundException extends RuntimeException {

  public RestaurantNotFoundException(String restaurantName) {
    super("Restaurant with Name " + restaurantName + " Not Found");
  }

  public HttpStatus getHttpStatus() {
    return HttpStatus.NOT_FOUND;
  }
}
