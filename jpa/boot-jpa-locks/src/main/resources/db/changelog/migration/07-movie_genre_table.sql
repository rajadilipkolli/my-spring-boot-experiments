create table movie_genre (movie_id bigint not null, genre_id bigint not null);
alter table if exists movie_genre add constraint FK86p3roa187k12avqfl28klp1q foreign key (genre_id) references genres;
alter table if exists movie_genre add constraint FKg7f38h6umffo51no9ywq91438 foreign key (movie_id) references movies;
