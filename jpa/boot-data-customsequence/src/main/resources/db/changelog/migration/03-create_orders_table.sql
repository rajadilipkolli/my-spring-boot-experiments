-- liquibase formatted sql
-- changeset author:app id:create-table-orders
-- see https://docs.liquibase.com/concepts/changelogs/sql-format.html

create sequence orders_seq start with 1 increment by 50;

create table orders (
    id varchar(10) not null,
    text varchar(1024) not null,
    customer_id varchar(10) not null,
    primary key (id)
);
