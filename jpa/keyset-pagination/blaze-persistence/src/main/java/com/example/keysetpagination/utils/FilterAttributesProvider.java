package com.example.keysetpagination.utils;

import com.blazebit.text.FormatUtils;
import com.blazebit.text.SerializableFormat;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FilterAttributesProvider {

    // Cache for already constructed filter attributes for specific entity classes
    private final Map<Class<?>, Map<String, SerializableFormat<? extends Serializable>>> cache =
            new ConcurrentHashMap<>();

    public Map<String, SerializableFormat<? extends Serializable>> getFilterAttributes(Class<?> entityClass) {
        return cache.computeIfAbsent(entityClass, this::createDynamicFilterAttributes);
    }

    private Map<String, SerializableFormat<? extends Serializable>> createDynamicFilterAttributes(
            Class<?> entityClass) {
        Map<String, SerializableFormat<? extends Serializable>> attributes = new HashMap<>();
        Class<?> currentClass = entityClass;
        while (currentClass != null) {
            for (Field field : entityClass.getDeclaredFields()) {
                Class<?> fieldType = field.getType();
                SerializableFormat<? extends Serializable> format = getFormatForType(fieldType);
                if (format != null) {
                    attributes.put(field.getName(), format);
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        return Collections.unmodifiableMap(attributes);
    }

    private SerializableFormat<? extends Serializable> getFormatForType(Class<?> fieldType) {
        return FormatUtils.getAvailableFormatters().get(fieldType); // Retrieve format based on the fieldType
    }
}
