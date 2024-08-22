create sequence movies_seq start with 1 increment by 1;

create table movies (
    id bigint DEFAULT nextval('movies_seq') not null,
    title text not null,
    primary key (id)
);
