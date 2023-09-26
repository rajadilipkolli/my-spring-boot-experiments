package com.example.opensearch.model.response;

import org.springframework.data.geo.Point;

public record ResultData(String name, Point location, Double dist) {}
