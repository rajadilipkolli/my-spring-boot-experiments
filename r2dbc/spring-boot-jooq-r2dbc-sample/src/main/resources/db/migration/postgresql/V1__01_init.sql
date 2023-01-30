CREATE TABLE posts
(
    ID uuid NOT NULL,
    TITLE text,
    CONTENT text,
    STATUS varchar(50),
    created_at timestamp,
    created_by text,
    updated_at timestamp,
    version BIGINT,
    PRIMARY KEY   (ID)
);

create table post_comments
(
    id uuid not null,
    content text,
    created_at timestamp,
    POST_ID uuid,
    primary key (id),
    CONSTRAINT FK_POST_COMMENTS FOREIGN KEY (POST_ID) REFERENCES POSTS(ID)
);

create table tags
(
    id uuid not null,
    name text,
    primary key (id)
);

create table posts_tags
(
    post_id uuid not null,
    tag_id uuid not null
);
