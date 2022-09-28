
create table   articles
(
    id       serial primary key,
    title    varchar(255) not null,
    authored timestamp    not null
);


create table   comments
(
    id         serial primary key,
    comment    varchar(255) not null,
    article_id bigint,
    constraint article_fk foreign key (article_id) references articles (id)
);

alter table articles add column published timestamp null;


insert into articles( title, authored, published) values( 'Waiter! There is a bug in my JSoup!' , now(), null);
insert into articles( title, authored, published) values( 'Beat the Queue with Apache Kafka' , now(), null);
insert into articles( title, authored, published) values( 'How I Stopped Worrying and Learned to Devops My SQL' , now(), NOW());
insert into comments (comment, article_id)  values ('first!' , (select max(id) from articles));
insert into comments (comment, article_id)  values ('first!' , (select min(id) from articles));
insert into comments (comment, article_id)  values ('i came here to say that.' , (select min(id) from articles));