CREATE SEQUENCE sequence_generator
  START WITH 1000
  INCREMENT BY 1;

CREATE TABLE CardHolder
(
    id BIGINT NOT NULL,
    member_id VARCHAR(255) NOT NULL,
    card_number VARCHAR(255) NOT NULL,
    PRIMARY KEY   (ID)
);
