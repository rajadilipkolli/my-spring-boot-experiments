-- liquibase formatted sql
-- changeset author:author id:001-init
-- see https://docs.liquibase.com/concepts/changelogs/sql-format.html

create sequence orders_seq start with 1 increment by 50;

create table orders (
    id bigint DEFAULT nextval(`orders_seq`) not null,
    text varchar(1024) not null,
    primary key (id)
);
