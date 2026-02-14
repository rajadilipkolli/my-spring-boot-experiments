CREATE TABLE post_comments
(
    id              BIGINT                      NOT NULL,
    comment_ref_id  BIGINT                      NOT NULL,
    title           TEXT                        NOT NULL,
    content         TEXT,
    published       BOOLEAN                     NOT NULL,
    published_at    TIMESTAMP WITHOUT TIME ZONE,
    version         SMALLINT                    NOT NULL DEFAULT 0,
    post_id         BIGINT                      NOT NULL,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    modified_at     TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_post_comments PRIMARY KEY (id)
);

ALTER TABLE post_comments
    ADD CONSTRAINT FK_POST_COMMENTS_ON_POST FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE;

create sequence IF NOT EXISTS post_comments_seq start with 1 increment by 50;

CREATE UNIQUE INDEX IF NOT EXISTS uc_postcommententity_title ON post_comments (title, post_id);