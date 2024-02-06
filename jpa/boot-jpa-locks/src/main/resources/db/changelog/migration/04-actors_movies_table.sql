create table actors_movies (actors_actor_id bigint not null, movies_movie_id bigint not null);
alter table if exists actors_movies add constraint FKqdd6w03v1gtdoia3cvfh0kr1r foreign key (movies_movie_id) references movies;
alter table if exists actors_movies add constraint FKc7mh1y5s0sm4mps3y8p106y2f foreign key (actors_actor_id) references actors;