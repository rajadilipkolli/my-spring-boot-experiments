--liquibase formatted sql

--changeset raja:create-sequence
CREATE SEQUENCE sequence_generator START WITH 1000 INCREMENT BY 1;