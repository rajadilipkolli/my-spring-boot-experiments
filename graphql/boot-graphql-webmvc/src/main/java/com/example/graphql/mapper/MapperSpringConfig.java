package com.example.graphql.mapper;

import org.mapstruct.MapperConfig;
import org.mapstruct.extensions.spring.SpringMapperConfig;

@MapperConfig(uses = ConversionServiceAdapter.class)
@SpringMapperConfig(conversionServiceBeanName = "appConversionService")
public class MapperSpringConfig {}
