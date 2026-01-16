CREATE TABLE tags
(
    id              BIGINT       NOT NULL,
    tag_name        VARCHAR(255) NOT NULL,
    tag_description VARCHAR(255),
    CONSTRAINT pk_tags PRIMARY KEY (id)
);

ALTER TABLE tags
    ADD CONSTRAINT uc_tag_name UNIQUE (tag_name);

create sequence IF NOT EXISTS tags_seq start with 1 increment by 50;