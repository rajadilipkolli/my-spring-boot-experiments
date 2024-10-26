package com.example.keysetpagination.utils;

import com.blazebit.text.FormatUtils;
import com.blazebit.text.SerializableFormat;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;

@Getter
public class FilterAttributesProvider {

    // Cache for already constructed filter attributes for specific entity classes
    private final Map<Class<?>, Map<String, SerializableFormat<? extends Serializable>>> cache =
            new ConcurrentHashMap<>();

    public Map<String, SerializableFormat<? extends Serializable>> getFilterAttributes(
            Class<?> entityClass) {
        return cache.computeIfAbsent(entityClass, this::createDynamicFilterAttributes);
    }

    private Map<String, SerializableFormat<? extends Serializable>> createDynamicFilterAttributes(
            Class<?> entityClass) {
        Map<String, SerializableFormat<? extends Serializable>> attributes = new HashMap<>();

        for (Field field : entityClass.getDeclaredFields()) {
            Class<?> fieldType = field.getType();
            SerializableFormat<? extends Serializable> format = getFormatForType(fieldType);
            if (format != null) {
                attributes.put(field.getName(), format);
            }
        }
        return Collections.unmodifiableMap(attributes);
    }

    private SerializableFormat<? extends Serializable> getFormatForType(Class<?> fieldType) {
        if (fieldType.equals(Long.class)) {
            return FormatUtils.getAvailableFormatters().get(Long.class);
        } else if (fieldType.equals(String.class)) {
            return FormatUtils.getAvailableFormatters().get(String.class);
        } else if (fieldType.equals(LocalDate.class)) {
            return FormatUtils.getAvailableFormatters().get(LocalDate.class);
        }
        // Add other supported types as necessary
        return null;
    }
}
