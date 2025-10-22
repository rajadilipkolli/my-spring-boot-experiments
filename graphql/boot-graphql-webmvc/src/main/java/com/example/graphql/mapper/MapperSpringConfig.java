package com.example.graphql.mapper;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.extensions.spring.SpringMapperConfig;

@MapperConfig(unmappedTargetPolicy = ReportingPolicy.IGNORE)
@SpringMapperConfig(conversionServiceBeanName = "appConversionService")
public class MapperSpringConfig {}
