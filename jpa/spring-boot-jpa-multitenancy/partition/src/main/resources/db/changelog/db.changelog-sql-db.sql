CREATE SEQUENCE  IF NOT EXISTS customers_seq START WITH 1 INCREMENT BY 50;

create TABLE if not exists CUSTOMERS (
  id bigint not null default nextval('customers_seq'),
  text varchar(255),
  tenant varchar(255) not null,
  primary key (id, tenant)
) PARTITION BY LIST (tenant);

CREATE TABLE if not exists customers_dbsystc PARTITION OF customers FOR VALUES IN ('dbsystc');
CREATE TABLE if not exists customers_def PARTITION OF customers DEFAULT;
