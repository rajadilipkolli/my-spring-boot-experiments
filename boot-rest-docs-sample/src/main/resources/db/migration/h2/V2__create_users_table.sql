create sequence users_seq start with 1 increment by 50;

create table users (
    id bigint DEFAULT nextval('users_seq') not null,
    text varchar(1024) not null,
    primary key (id)
);
