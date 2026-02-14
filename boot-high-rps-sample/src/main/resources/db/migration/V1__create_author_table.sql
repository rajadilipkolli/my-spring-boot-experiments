CREATE TABLE authors
(
    id            BIGINT        
    first_name    TEXT NOT NULL,
    middle_name   TEXT,
    last_name     TEXT NOT NULL,
    mobile        BIGINT        NOT NULL,
    email         TEXT NOT NULL,
    registered_at TIMESTAMP WITHOUT TIME ZONE,
    version       SMALLINT      NOT NULL DEFAULT 0,
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    modified_at   TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_authors PRIMARY KEY (id)
);

ALTER TABLE authors
    ADD CONSTRAINT uc_authors_email UNIQUE (email);

create sequence IF NOT EXISTS authors_seq start with 1 increment by 50;