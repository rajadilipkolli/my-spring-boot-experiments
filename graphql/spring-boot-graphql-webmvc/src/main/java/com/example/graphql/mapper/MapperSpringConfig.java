package com.example.graphql.mapper;

import com.example.graphql.mapper.adapter.ConversionServiceAdapter;
import org.mapstruct.MapperConfig;
import org.mapstruct.extensions.spring.SpringMapperConfig;

@MapperConfig(uses = ConversionServiceAdapter.class)
@SpringMapperConfig(conversionServiceAdapterPackage = "com.example.graphql.mapper.adapter")
public class MapperSpringConfig {}
