create sequence posts_seq start with 1 increment by 50;

create table posts (
    id bigint DEFAULT nextval('posts_seq') not null,
    text varchar(1024) not null,
    primary key (id)
);
