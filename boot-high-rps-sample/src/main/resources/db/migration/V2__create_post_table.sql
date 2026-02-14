CREATE TABLE posts
(
    id           BIGINT                      NOT NULL,
    post_ref_id  BIGINT                      NOT NULL,
    title        VARCHAR(255),
    content      VARCHAR(4096),
    published    BOOLEAN                     NOT NULL,
    published_at TIMESTAMP WITHOUT TIME ZONE,
    author_id    BIGINT,
    version      SMALLINT                    NOT NULL DEFAULT 0,
    created_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    modified_at  TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_posts PRIMARY KEY (id)
);

ALTER TABLE posts
    ADD CONSTRAINT FK_POSTS_ON_AUTHOR FOREIGN KEY (author_id) REFERENCES authors (id);

create sequence IF NOT EXISTS posts_seq start with 1 increment by 50;

CREATE UNIQUE INDEX IF NOT EXISTS uc_postentity_title_author_id ON posts (title, author_id);