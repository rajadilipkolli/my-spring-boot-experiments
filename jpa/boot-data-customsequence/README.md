# Custom Sequence Generation

Custom sequence in Hibernate refers to the ability to specify a custom sequence generator for generating unique values for the primary keys of entities. This allows users to define their own sequence generation strategy, rather than relying on the default strategy provided by Hibernate. Custom sequence generators can be useful for ensuring that primary keys are generated in a specific format or based on specific criteria.

### Run tests
`./mvnw clean verify`

### Run locally
```shell
docker-compose -f docker/docker-compose.yml up -d
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```


### Useful Links
* Swagger UI: http://localhost:8080/swagger-ui.html
* Actuator Endpoint: http://localhost:8080/actuator

### Notes
* ~~MariaDb doesn't support pooled-lo algorithm for fetching sequences and store in db, so for each insert it will fetch next sequence value.~~

### Validation Groups (`GroupCheck` vs `SkipGroupCheck`)

This module demonstrates contextual payload validation using Jakarta Bean Validation groups defined in [`ValidationGroups`](src/main/java/com/example/custom/sequence/model/request/ValidationGroups.java):

```java
public interface ValidationGroups {
    interface SkipGroupCheck {}
    interface GroupCheck {}
}
```

#### How it works:
1. **Targeted Constraint**:
   In [`OrderRequest`](src/main/java/com/example/custom/sequence/model/request/OrderRequest.java), the `customerId` field is constrained to the `GroupCheck` group:
   ```java
   public record OrderRequest(
       @NotBlank(message = "Item name cannot be blank") String itemName,
       @NotBlank(message = "CustomerId cannot be blank", groups = ValidationGroups.GroupCheck.class) String customerId
   ) {}
   ```
   - Constraints without an explicit group (like `itemName`) belong to `jakarta.validation.groups.Default`.
   - `customerId` is only validated when `ValidationGroups.GroupCheck` is explicitly activated.

2. **Skipping Customer ID Validation on Customer Creation**:
   When creating a new customer via `POST /api/customers`, the customer does not have an ID assigned yet. Orders submitted together in `CustomerRequest` cannot specify an existing `customerId`.
   `CustomerController.createCustomer` validates using:
   ```java
   @RequestBody @Validated(value = {Default.class, ValidationGroups.SkipGroupCheck.class}) CustomerRequest customerRequest
   ```
   Because `GroupCheck` is not included in the `@Validated` list, validation for `customerId` in nested `OrderRequest` items is skipped.

3. **Enforcing Customer ID Validation on Updates and Direct Order Operations**:
   - **Customer Update (`PUT /api/customers/{id}`)**: Customer already exists, so any nested orders must be linked to a customer.
     ```java
     @RequestBody @Validated(value = {Default.class, ValidationGroups.GroupCheck.class}) CustomerRequest customerRequest
     ```
   - **Order Endpoints (`POST /api/orders`, `PUT /api/orders/{id}`)**: Standalone order creation and updates require the owning customer ID.
     ```java
     @RequestBody @Validated(value = {Default.class, ValidationGroups.GroupCheck.class}) OrderRequest orderRequest
     ```
   Because `ValidationGroups.GroupCheck` is included, `@NotBlank` on `customerId` is enforced, rejecting payloads where `customerId` is missing or blank with HTTP 400 Bad Request.

