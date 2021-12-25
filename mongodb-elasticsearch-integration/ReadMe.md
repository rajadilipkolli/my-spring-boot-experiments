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

## Useful commands

 - To view elasticsearch stats for a document http://localhost:9200/restaurant?pretty
 - To retrieve specific document from index http://localhost:9200/restaurant/_doc/61c75c9dab6a3e6abf8a3179?pretty
 - 