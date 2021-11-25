package com.example.mongoes.mongodb.eventlistener;

import com.example.mongoes.elasticsearch.domain.ERestaurant;
import com.example.mongoes.elasticsearch.repository.ERestaurantRepository;
import com.example.mongoes.mongodb.customannotation.CascadeSaveList;
import com.example.mongoes.mongodb.domain.Restaurant;
import com.example.mongoes.utils.ApplicationConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.core.geo.GeoJsonPoint;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;

@Slf4j
public class CascadeSaveMongoEventListener<E> extends AbstractMongoEventListener<E> {

  private final MongoOperations mongoOperations;

  private final ERestaurantRepository eRestaurantRepository;

  public CascadeSaveMongoEventListener(MongoOperations reactiveMongoOperations, ERestaurantRepository eRestaurantRepository) {
    this.mongoOperations = reactiveMongoOperations;
    this.eRestaurantRepository = eRestaurantRepository;
  }

  @Override
  public void onBeforeConvert(BeforeConvertEvent<E> event) {
    ReflectionUtils.doWithFields(
        event.getSource().getClass(),
            new CascadeCallback(event.getSource(), mongoOperations));
  }

  @Override
  public void onAfterSave(AfterSaveEvent<E> event) {

      log.debug("onAfterSave({}, {})", event.getSource(), event.getDocument());
      if (ApplicationConstants.RESTAURANT_COLLECTION.equals(event.getCollectionName())) {
         ERestaurant eRestaurant = convertToERestaurant(event.getSource());
         this.eRestaurantRepository.save(eRestaurant)
                 .subscribe(eRestaurant1 -> log.info("Saved in ElasticSearch :{}", eRestaurant1));
      }

  }

  private ERestaurant convertToERestaurant(E source) {
    Restaurant restaurant = (Restaurant) source;
    return ERestaurant.builder().restaurantName(restaurant.getRestaurantName())
//            .location(GeoJsonPoint.of(restaurant.getLocation().getX(), restaurant.getLocation().getY()))
            .borough(restaurant.getBorough())
            .id(restaurant.getId()).build();
  }

  private record CascadeCallback(Object source,
                                 MongoOperations mongoOperations1) implements ReflectionUtils.FieldCallback {

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
        mongoOperations1.save(fieldValue);
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
