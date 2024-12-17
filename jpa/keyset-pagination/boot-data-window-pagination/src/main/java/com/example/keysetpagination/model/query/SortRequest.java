package com.example.keysetpagination.model.query;

public class SortRequest {

    private String field;
    private String direction;

    /**
 * Gets the field value.
 *
 * @return the field value as a String
 */
public String getField() {
    return field;
}

    /**
 * Sets the field value.
 *
 * @param field the value to set
 */
public void setField(String field) {
    this.field = field;
}

    /**
 * Gets the current direction.
 *
 * @return the direction as a String
 */
public String getDirection() {
    return direction;
}

    /**
 * Sets the direction of movement.
 *
 * @param direction the direction to set
 */
public void setDirection(String direction) {
    this.direction = direction;
}
}
