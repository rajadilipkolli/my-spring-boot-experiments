-- Init script

-- DDL
CREATE TABLE card_holder (
  id INT AUTO_INCREMENT PRIMARY KEY,
  member_id VARCHAR(255) NOT NULL,
  card_number VARCHAR(255) NOT NULL
);

-- Requires only if sequence type is Auto
create table hibernate_sequence(
    next_val INTEGER NOT null
);
