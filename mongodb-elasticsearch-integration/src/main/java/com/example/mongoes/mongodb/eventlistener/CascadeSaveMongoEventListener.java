package com.example.mongoes.mongodb.eventlistener;

import com.example.mongoes.elasticsearch.domain.ENotes;
import com.example.mongoes.elasticsearch.domain.ERestaurant;
import com.example.mongoes.mongodb.customannotation.CascadeSaveList;
import com.example.mongoes.utils.ApplicationConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterDeleteEvent;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class CascadeSaveMongoEventListener<E> extends AbstractMongoEventListener<E> {

  private final MongoOperations mongoOperations;
  private final ReactiveElasticsearchOperations reactiveElasticsearchOperations;
  private final ObjectMapper objectMapper;
  private final Map<String, Class<?>> classMap = new HashMap<>();

  public CascadeSaveMongoEventListener(MongoOperations reactiveMongoOperations, ReactiveElasticsearchOperations reactiveElasticsearchOperations, ObjectMapper objectMapper) {
    this.mongoOperations = reactiveMongoOperations;
    this.reactiveElasticsearchOperations = reactiveElasticsearchOperations;
    this.objectMapper = objectMapper;
    populateMap();
  }

  private void populateMap() {
    classMap.put(ApplicationConstants.RESTAURANT_COLLECTION, ERestaurant.class);
    classMap.put(ApplicationConstants.NOTE_COLLECTION, ENotes.class);
  }

  @Override
  public void onBeforeConvert(BeforeConvertEvent<E> event) {
    log.debug("onBeforeConvert({})", event.getSource());
    ReflectionUtils.doWithFields(
        event.getSource().getClass(),
            new CascadeCallback(event.getSource(), mongoOperations));
  }

  @SneakyThrows
  @Override
  public void onAfterSave(AfterSaveEvent<E> event) {

      log.debug("onAfterSave({}, {})", event.getSource(), event.getDocument());
      if (ApplicationConstants.RESTAURANT_COLLECTION.equals(event.getCollectionName())) {
          String json = objectMapper.writeValueAsString(event.getSource());
          var tClass = getClassByCollectionName(event.getCollectionName());
          reactiveElasticsearchOperations.save(objectMapper.readValue(json, tClass), IndexCoordinates.of(event.getCollectionName()))
                 .subscribe(persistedRepository -> log.info("Saved in ElasticSearch :{}", persistedRepository));
      }
  }

  @Override
  public void onAfterDelete(AfterDeleteEvent<E> event) {

      log.debug("onAfterDelete({})", event.getDocument());
      this.reactiveElasticsearchOperations.delete(event.getSource().getString("id"), IndexCoordinates.of(event.getCollectionName()))
              .subscribe();
  }

  private Class<?> getClassByCollectionName(String collectionName) {
    return classMap.get(collectionName);
  }

  private record CascadeCallback(Object source,
                                 MongoOperations mongoOperations) implements ReflectionUtils.FieldCallback {

    @Override
    public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
      ReflectionUtils.makeAccessible(field);

      if (field.isAnnotationPresent(DBRef.class)
              && field.isAnnotationPresent(CascadeSaveList.class)) {
        final List<Object> fieldValueList = (List<Object>) field.get(source);
        if (fieldValueList != null && !fieldValueList.isEmpty()) {
          for (Object fieldValue : fieldValueList) {
            checkAndCreateIDIfNotExists(fieldValue);
          }
        }
      }
    }

    private void checkAndCreateIDIfNotExists(Object fieldValue) {
      if (fieldValue != null) {
        DbRefFieldCallback dbRefFieldCallback = new DbRefFieldCallback();

        ReflectionUtils.doWithFields(fieldValue.getClass(), dbRefFieldCallback);

        if (!dbRefFieldCallback.isIdFound()) {
          throw new MappingException("Cannot perform cascade save on child object without id set");
        }
        mongoOperations.save(fieldValue);
      }
    }
  }

  private static class DbRefFieldCallback implements ReflectionUtils.FieldCallback {

    private boolean idFound;

    public void doWith(Field field) {
      ReflectionUtils.makeAccessible(field);

      if (field.isAnnotationPresent(Id.class)) {
        idFound = true;
      }
    }

    public boolean isIdFound() {
      return idFound;
    }
  }
}
