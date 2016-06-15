--liquibase formatted sql

--changeset tyler:1

create table user_ (
    id bigserial primary key,
    name varchar(255) not null unique,
    created timestamp default current_timestamp
);

CREATE TABLE full_user (
    id bigserial primary key,
    user_id bigint not null,
    email varchar(255) unique,
    password_encrypted varchar(512),
    created timestamp default current_timestamp,
    constraint fk1_user foreign key (user_id)
        references user_ (id)
        on update cascade on delete cascade
);

CREATE TABLE login (
    id bigserial primary key,
    user_id bigint not null,
    auth VARCHAR(255) not null,
    expire_time timestamp default current_timestamp,
    created timestamp default current_timestamp,
    constraint fk1_user foreign key (user_id)
        references user_ (id)
        on update cascade on delete cascade
);

create table discussion (
    id bigserial primary key,
    subject varchar(140) not null,
    created timestamp default current_timestamp
);

create table comment (
    id bigserial primary key,
    user_id bigint not null,
    discussion_id bigint not null,
    text_ text not null,
    created timestamp default current_timestamp,
    modified timestamp,
    constraint fk1_user foreign key (user_id)
        references user_ (id)
        on update cascade on delete cascade,
    constraint fk1_discussion foreign key (discussion_id)
        references discussion (id)
        on update cascade on delete cascade
);

create table comment_tree (
    id bigserial primary key,
    parent_id bigint not null,
    child_id bigint not null,
    path_length integer not null,
    created timestamp default current_timestamp,
    constraint fk1_parent_id foreign key (parent_id)
        references comment (id)
        on update cascade on delete cascade,
    constraint fk2_child_id foreign key (child_id)
        references comment (id)
        on update cascade on delete cascade
);

create table comment_rank (
    id bigserial primary key,
    comment_id bigint not null,
    user_id bigint not null,
    rank smallint,
    created timestamp default current_timestamp,
    constraint fk1_comment_id foreign key (comment_id)
        references comment (id)
        on update cascade on delete cascade,
    constraint fk2_user_id foreign key (user_id)
        references user_ (id)
        on update cascade on delete cascade
);

insert into user_ (name)
    values ('user_1'),('user_2'),('user_3');

insert into discussion (subject)
    values ('Cats'),('Dogs');

insert into comment (text_, user_id, discussion_id)
	values ('Node 1',1,1),('Node 1.1',2,1),('Node 2',3,1),('Node 1.1.1',2,1),('Node 2.1',1,1),
	('Node 1.2',2,1);

insert into comment_rank (comment_id, user_id, rank)
    values (1, 2, 40), (1, 3, 75), (2, 1, 100);

insert into comment_tree (parent_id, child_id, path_length)
	values 	  (1,1,0), (1,2,1), (1,4,2), (1,6,1),
              (2,2,0), (2,4,1),
              (3,3,0), (3,5,1),
              (4,4,0),
              (5,5,0),
              (6,6,0);



 --rollback drop table login cascade; drop table full_user cascade; drop table comment cascade ; drop table user_ cascade; drop table comment_tree cascade; drop table discussion cascade; drop table comment_rank cascade;


