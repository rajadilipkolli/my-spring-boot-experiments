create table customers (
    id bigint not null auto_increment,
    text varchar(1024) not null,
    name varchar(1024),
    zip_code int,
    primary key (id)
);
