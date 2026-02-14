CREATE TABLE tags
(
    id              BIGINT       NOT NULL,
    tag_name        VARCHAR(255) NOT NULL,
    tag_description VARCHAR(255),
    version         SMALLINT     NOT NULL DEFAULT 0,
    created_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    modified_at  TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_tags PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uc_tag_name ON tags (LOWER(tag_name));

create sequence IF NOT EXISTS tags_seq start with 1 increment by 5;