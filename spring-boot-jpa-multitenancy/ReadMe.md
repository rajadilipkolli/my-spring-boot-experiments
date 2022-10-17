## MultiTenancy using Hibernate in Spring Data JPA

This is the parent project for a couple of examples demonstrating how to integrate Hibernates Multitenant feature with Spring Data JPA in Spring Boot.

There are three modules for the three examples.

Each uses a different strategy to separate data by tenant:

 - Partition tables by tenant id.

 - Use a separate schema per tenant

 - Use a separate database per tenant.
