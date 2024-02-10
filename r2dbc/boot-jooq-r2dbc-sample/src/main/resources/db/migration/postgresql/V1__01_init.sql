CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE posts
(
    ID uuid NOT NULL DEFAULT uuid_generate_v4 (),
    TITLE text,
    CONTENT text,
    STATUS varchar(50),
    created_at timestamptz DEFAULT NOW(),
    created_by text,
    updated_at timestamptz,
    version int DEFAULT 0,
    PRIMARY KEY   (ID)
);

create table post_comments
(
    id uuid not null DEFAULT uuid_generate_v4 (),
    content text,
    created_at timestamptz DEFAULT NOW(),
    POST_ID uuid,
    primary key (id),
    CONSTRAINT FK_POST_COMMENTS FOREIGN KEY (POST_ID) REFERENCES POSTS(ID)
);

create table tags
(
    id uuid not null DEFAULT uuid_generate_v4 (),
    name text unique,
    created_at timestamptz DEFAULT NOW(),
    primary key (id)
);

CREATE TABLE posts_tags
(
    post_id UUID NOT NULL,
    tag_id UUID NOT NULL,
    CONSTRAINT FK_POST_TAGS_PID FOREIGN KEY (post_id) REFERENCES posts(id),
    CONSTRAINT FK_POST_TAGS_TID FOREIGN KEY (tag_id) REFERENCES tags(id),
    CONSTRAINT UK_POST_TAGS UNIQUE (post_id, tag_id)
);

