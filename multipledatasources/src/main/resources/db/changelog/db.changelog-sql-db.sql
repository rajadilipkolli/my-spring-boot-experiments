--liquibase formatted sql

--changeset raja:create-sequence
CREATE SEQUENCE hibernate_sequence START WITH 1000 INCREMENT BY 1;