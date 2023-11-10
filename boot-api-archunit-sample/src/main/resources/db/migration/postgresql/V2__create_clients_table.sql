create sequence clients_seq start with 1 increment by 50;

create table clients (
    id bigint DEFAULT nextval('clients_seq') not null,
    text text not null,
    primary key (id)
);
