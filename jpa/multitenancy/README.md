## MultiTenancy using Hibernate in Spring Data JPA

 - MultiTenancy is a feature of JPA that allows you to store database data across multiple tenants. It's not a new concept—many applications have used it for years to separate data by tenant. However, Spring Data JPA provides an easy way to use multi-tenancy in your application by using Hibernate as your persistence provider.

 - This allows you to create a single entity class that can be shared by multiple tenants, giving each tenant its own independent instance of the data.

Let's see how it works by using three different approaches:

| MultiTenancy Type                                       | Description                                                                    |
|---------------------------------------------------------|--------------------------------------------------------------------------------|
| [Partition tables by tenant id](./partition)            | Single table having partition (Supported Only from Hibernate 6)                |
| [Use a separate schema per tenant](./schema)            | Single Database having multiple schemas                                        |
| [Use a separate database per tenant](./multitenancy-db) | Different Databases having same schema (Could be having different aws regions) |

Additionally another sample which implments Multiple Datasoures and MultiTenancy

[Multi DataSource and Tenancy using schema and db](./multidatasource-multitenancy)
