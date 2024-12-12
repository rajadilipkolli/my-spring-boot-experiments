-- Init script

-- DDL
CREATE TABLE card_holder (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  member_id VARCHAR(255) NOT NULL,
  card_number VARCHAR(255) NOT NULL
);
