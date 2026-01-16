CREATE TABLE post_tag
(
    created_on TIMESTAMP WITHOUT TIME ZONE,
    post_id    BIGINT NOT NULL,
    tag_id     BIGINT NOT NULL,
    CONSTRAINT pk_post_tag PRIMARY KEY (post_id, tag_id)
);

ALTER TABLE post_tag
    ADD CONSTRAINT FK_POST_TAG_ON_POST FOREIGN KEY (post_id) REFERENCES posts (id);

ALTER TABLE post_tag
    ADD CONSTRAINT FK_POST_TAG_ON_TAG FOREIGN KEY (tag_id) REFERENCES tags (id);