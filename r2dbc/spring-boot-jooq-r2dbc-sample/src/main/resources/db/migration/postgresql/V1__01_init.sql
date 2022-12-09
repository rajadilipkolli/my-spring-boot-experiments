CREATE TABLE POST
(
    ID uuid NOT NULL,
    TITLE VARCHAR(255),
    CONTENT VARCHAR(255),
    STATUS varchar(50),
    created_at timestamp,
    created_by varchar(255),
    updated_at timestamp,
    version BIGINT,
    PRIMARY KEY   (ID)
);

create table POST_COMMENT
(
    id uuid not null,
    content varchar(255),
    created_at timestamp,
    POST_ID uuid,
    primary key (id),
    CONSTRAINT FK_POST_COMMENT FOREIGN KEY (POST_ID) REFERENCES POST(ID)
);

create table tags
(
    id uuid not null,
    name varchar(255),
    primary key (id)
);

create table posts_tags
(
    post_id uuid not null,
    tag_id uuid not null
);
