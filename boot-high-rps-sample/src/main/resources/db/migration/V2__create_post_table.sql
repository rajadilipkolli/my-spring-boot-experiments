CREATE TABLE posts
(
    id           BIGINT                      NOT NULL,
    title        VARCHAR(255),
    content      VARCHAR(4096),
    published    BOOLEAN                     NOT NULL,
    published_at TIMESTAMP WITHOUT TIME ZONE,
    author_id    BIGINT,
    created_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    modified_at  TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_posts PRIMARY KEY (id)
);

ALTER TABLE posts
    ADD CONSTRAINT FK_POSTS_ON_AUTHOR FOREIGN KEY (author_id) REFERENCES authors (id);

create sequence IF NOT EXISTS posts_seq start with 1 increment by 50;