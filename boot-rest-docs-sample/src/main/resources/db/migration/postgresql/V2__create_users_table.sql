create sequence users_seq start with 1 increment by 50;

create table users (
    id bigint DEFAULT nextval('users_seq') not null,
    first_name text not null,
    last_name text,
    age serial not null,
    gender text,
    phone_number text,
    primary key (id)
);
