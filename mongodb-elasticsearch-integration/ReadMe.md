## Swagger URI

> http://localhost:8080/swagger-ui.html

## Tools used
- Spring Data MongoDB
- Spring Data ElasticSearch
- Swagger (OpenAPI)
- Lombok

## Steps to save the documents in MongoDb and elasticSearch

- Use `@DBRef` to link the 1-n documents
- Create custom annotation `@CascadeSaveList` which is triggered at runTime using the custom Listener(`CascadeSaveMongoEventListener`) which extends`AbstractMongoEventListener`
- Enable AuditLogging
- Use `AfterConvertEvent` to trigger saving to ElasticSearch
- ElasticSearch uses NestedField to save 1-n relationShip