CREATE TABLE post_details
(
    id          BIGINT                      NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    modified_at TIMESTAMP WITHOUT TIME ZONE,
    details_key VARCHAR(255),
    created_by  VARCHAR(255),
    CONSTRAINT pk_post_details PRIMARY KEY (id)
);

ALTER TABLE post_details
    ADD CONSTRAINT FK_POST_DETAILS_ON_ID FOREIGN KEY (id) REFERENCES posts (id);