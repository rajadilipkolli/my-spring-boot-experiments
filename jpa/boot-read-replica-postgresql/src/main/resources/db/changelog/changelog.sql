--liquibase formatted sql

--changeset appUser:1
--preconditions onFail:MARK_RAN, onError:MARK_RAN,
CREATE SEQUENCE IF NOT EXISTS articles_seq
    INCREMENT 50
    START 1;
--rollback DROP SEQUENCE IF EXISTS articles_seq;

--changeset appUser:2
--preconditions onFail:MARK_RAN, onError:MARK_RAN,
CREATE SEQUENCE IF NOT EXISTS comments_seq
    INCREMENT 50
    START 1;
--rollback DROP SEQUENCE IF EXISTS comment_seq;

--changeset appUser:3
--preconditions onFail:MARK_RAN onError:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*)   from information_schema.tables where table_name = 'articles' ;

--  HALT, CONTINUE, MARK_RAN, WARN

create table   articles
(
    id       bigint DEFAULT nextval('articles_seq'),
    title    varchar(255) not null,
    authored timestamp    not null,
    CONSTRAINT "articles_pkey" PRIMARY KEY ("id")
);

--changeset appUser:4
--preconditions onFail:MARK_RAN, onError:MARK_RAN,
--precondition-sql-check expectedResult:0 SELECT COUNT(*) from information_schema.tables where table_name = 'comments' ;

create table   comments
(
    id         bigint primary key DEFAULT nextval('comments_seq'),
    comment    varchar(255) not null,
    article_id bigint,
    constraint article_fk foreign key (article_id) references articles (id) ON UPDATE NO ACTION ON DELETE NO ACTION
);
--rollback drop table comments, articles;

--changeset appUser:5
--preconditions onFail:MARK_RAN, onError:MARK_RAN,
--precondition-sql-check expectedResult:0 SELECT count(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'articles' and column_name = 'published';
alter table articles add column published timestamp;
--rollback alter table articles drop column published;

--changeset appUser:6
--preconditions onFail:MARK_RAN, onError:MARK_RAN,
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM articles;
insert into articles(title, authored, published) values('Waiter! There is a bug in my JSoup!' , now(), now());
insert into articles(title, authored, published) values('Beat the Queue with Apache Kafka' , now(), null);
insert into articles(title, authored, published) values('How I Stopped Worrying and Learned to Devops My SQL' , now(), null);
insert into comments (comment, article_id)  values ('first!' , (select max(id) from articles));
insert into comments (comment, article_id)  values ('first!' , (select min(id) from articles));
insert into comments (comment, article_id)  values ('i came here to say that.' , (select min(id) from articles));
--rollback truncate comments, articles;

--changeset raja:7
--preconditions onFail:MARK_RAN, onError:MARK_RAN,
GRANT SELECT ON ALL TABLES IN SCHEMA public TO repl_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA PUBLIC GRANT SELECT ON TABLES TO repl_user;