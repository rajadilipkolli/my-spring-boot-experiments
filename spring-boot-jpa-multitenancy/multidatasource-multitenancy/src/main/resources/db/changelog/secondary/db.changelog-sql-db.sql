create schema if not exists test1;
create schema if not exists test2;

create sequence test1.customers_seq start with 1 increment by 50;
create table test1.customers (id bigint not null, name varchar(255), primary key (id));

create sequence test2.customers_seq start with 1 increment by 50;
create table test2.customers (id bigint not null, name varchar(255), primary key (id));
