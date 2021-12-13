package com.example.mongoes.web.exception;

import com.example.mongoes.web.response.GenericMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ControllerExceptionHandler {

  @ExceptionHandler(RestaurantNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  ResponseEntity<Object> handleRestaurantNotFoundException(
      RestaurantNotFoundException restaurantNotFoundException) {
    return ResponseEntity.status(restaurantNotFoundException.getHttpStatus())
        .body(new GenericMessage(restaurantNotFoundException.getMessage()));
  }
}
