create sequence users_seq start with 1 increment by 50;

create table users (
    id bigint DEFAULT nextval('users_seq') not null,
    first_name varchar(1024) not null,
    last_name varchar(1024),
    age serial not null,
    gender varchar(6),
    phone_number varchar(10),
    primary key (id)
);
