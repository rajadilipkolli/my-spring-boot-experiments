CREATE TABLE post_comments
(
    id           BIGINT                      NOT NULL,
    created_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    modified_at  TIMESTAMP WITHOUT TIME ZONE,
    title        VARCHAR(255)                NOT NULL,
    content      VARCHAR(255),
    published    BOOLEAN                     NOT NULL,
    published_at TIMESTAMP WITHOUT TIME ZONE,
    post_id      BIGINT,
    CONSTRAINT pk_post_comments PRIMARY KEY (id)
);

ALTER TABLE post_comments
    ADD CONSTRAINT FK_POST_COMMENTS_ON_POST FOREIGN KEY (post_id) REFERENCES posts (id);

create sequence IF NOT EXISTS post_comments_seq start with 1 increment by 50;