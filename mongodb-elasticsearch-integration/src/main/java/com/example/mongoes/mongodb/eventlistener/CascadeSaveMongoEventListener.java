package com.example.mongoes.mongodb.eventlistener;

import com.example.mongoes.mongodb.customannotation.CascadeSaveList;
import org.springframework.data.annotation.Id;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;

public class CascadeSaveMongoEventListener<E> extends AbstractMongoEventListener<E> {

  private final ReactiveMongoOperations reactiveMongoOperations;

  public CascadeSaveMongoEventListener(ReactiveMongoOperations reactiveMongoOperations) {
    this.reactiveMongoOperations = reactiveMongoOperations;
  }

  @Override
  public void onBeforeConvert(BeforeConvertEvent<E> event) {
    ReflectionUtils.doWithFields(
        event.getSource().getClass(),
            new CascadeCallback(event.getSource(), reactiveMongoOperations));
  }

  private record CascadeCallback(Object source,
                                 ReactiveMongoOperations reactiveMongoOperations) implements ReflectionUtils.FieldCallback {

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

        reactiveMongoOperations.save(fieldValue).block();
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
