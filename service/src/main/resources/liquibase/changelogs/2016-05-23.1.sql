--liquibase formatted sql

--changeset tyler:1

create table user_ (
    id bigserial primary key,
    name varchar(255) not null unique,
    created timestamp default current_timestamp
);

CREATE TABLE full_user (
    id bigserial primary key,
    user_id bigserial not null,
    email varchar(255) unique,
    password_encrypted varchar(512),
    created timestamp default current_timestamp,
    constraint fk1_user foreign key (user_id)
        references user_ (id)
        on update cascade on delete cascade
);

CREATE TABLE login (
    id bigserial primary key,
    user_id bigserial not null,
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
    user_id bigserial not null,
    discussion_id bigserial not null,
    text_ text not null,
    created timestamp default current_timestamp,
    constraint fk1_user foreign key (user_id)
        references user_ (id)
        on update cascade on delete cascade
);

create table comment_tree (
    id bigserial primary key,
    parent_id bigserial not null,
    child_id bigserial not null,
    path_length integer not null,
    created timestamp default current_timestamp,
    constraint fk1_parent_id foreign key (parent_id)
        references comment (id)
        on update cascade on delete cascade,
    constraint fk2_child_id foreign key (child_id)
        references comment (id)
        on update cascade on delete cascade
);

insert into user_ (name)
    values ('user_1'),('user_2'),('user_3');

insert into discussion (subject)
    values ('Cats'),('Dogs');

insert into comment (text_, user_id, discussion_id)
	values ('Node 1',1,1),('Node 1.1',2,1),('Node 2',3,1),('Node 1.1.1',2,1),('Node 2.1',1,1),
	('Node 1.2',2,1);

insert into comment_tree (parent_id, child_id, path_length)
	values 	  (1,1,0), (1,2,1), (1,4,2), (1,6,1),
              (2,2,0), (2,4,1),
              (3,3,0), (3,5,1),
              (4,4,0),
              (5,5,0),
              (6,6,0);

 --rollback drop table login cascade; drop table full_user cascade; drop table comment cascade ; drop table user_ cascade; drop table comment_tree cascade; drop table discussion cascade;


