--liquibase formatted sql

--changeset appUser:1
--preconditions onFail:MARK_RAN onError:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*)   from information_schema.tables where table_name = 'articles' ;

--  HALT, CONTINUE, MARK_RAN, WARN

create table   articles
(
    id       serial primary key,
    title    varchar(255) not null,
    authored timestamp    not null
);

--changeset appUser:2
--preconditions onFail:MARK_RAN, onError:MARK_RAN,
--precondition-sql-check expectedResult:0 SELECT COUNT(*) from information_schema.tables where table_name = 'comments' ;

create table   comments
(
    id         serial primary key,
    comment    varchar(255) not null,
    article_id serial,
    constraint article_fk foreign key (article_id) references articles (id)
);
--rollback drop table comments, articles;

--changeset appUser:3
--preconditions onFail:MARK_RAN, onError:MARK_RAN,
--precondition-sql-check expectedResult:0 SELECT count(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'articles' and column_name = 'published';
alter table articles add column published timestamp null;
--rollback alter table articles drop column published;

--changeset appUser:4
--preconditions onFail:MARK_RAN, onError:MARK_RAN,
CREATE SEQUENCE IF NOT EXISTS articles_seq
    INCREMENT 50
    START 100
    MINVALUE 1
    MAXVALUE 2147483647
    CACHE 1
    OWNED BY articles.id;
--rollback DROP SEQUENCE IF EXISTS articles_seq;

--changeset appUser:5
--preconditions onFail:MARK_RAN, onError:MARK_RAN,
CREATE SEQUENCE IF NOT EXISTS comments_seq
    INCREMENT 50
    START 100
    MINVALUE 1
    MAXVALUE 2147483647
    CACHE 1
    OWNED BY comments.id;
--rollback DROP SEQUENCE IF EXISTS comment_seq;

--changeset appUser:6
--preconditions onFail:MARK_RAN, onError:MARK_RAN,
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM articles;
insert into articles( id, title, authored, published) values( 1, 'Waiter! There is a bug in my JSoup!' , now(), null);
insert into articles( id, title, authored, published) values( 2, 'Beat the Queue with Apache Kafka' , now(), null);
insert into articles( id, title, authored, published) values( 3, 'How I Stopped Worrying and Learned to Devops My SQL' , now(), null);
insert into comments (id, comment, article_id)  values (1, 'first!' , (select max(id) from articles));
insert into comments (id, comment, article_id)  values (2, 'first!' , (select min(id) from articles));
insert into comments (id, comment, article_id)  values (3, 'i came here to say that.' , (select min(id) from articles));
--rollback truncate comments, articles;

--changeset raja:7
--preconditions onFail:MARK_RAN, onError:MARK_RAN,
GRANT SELECT ON ALL TABLES IN SCHEMA public TO repl_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA PUBLIC GRANT SELECT ON TABLES TO repl_user;