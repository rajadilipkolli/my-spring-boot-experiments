--liquibase formatted sql

--changeset raja:create-sequence
CREATE SEQUENCE member_seq START WITH 101 INCREMENT BY 50;