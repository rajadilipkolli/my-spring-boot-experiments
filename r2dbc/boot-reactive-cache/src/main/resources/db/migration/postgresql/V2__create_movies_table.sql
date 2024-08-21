create sequence movies_seq start with 1 increment by 50;

create table movies (
    id bigint DEFAULT nextval('movies_seq') not null,
    text text not null,
    primary key (id)
);
