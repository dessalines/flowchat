--liquibase formatted sql

--changeset tyler:1

create table user_ (
    id bigserial primary key,
    name varchar(255) not null,
    created timestamp default current_timestamp
);

create table comment (
    id bigserial primary key,
    user_id bigserial not null,
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

insert into user_ (id, name)
    values (1,'user_1'),(2,'user_2'),(3,'user_3');

insert into comment (id, text_, user_id)
	values (1,'Node 1',1),(2,'Node 1.1',2),(3,'Node 2',3),(4,'Node 1.1.1',2),(5,'Node 2.1',1),
	(6,'Node 1.2',2);

insert into comment_tree (parent_id, child_id, path_length)
	values 	  (1,1,0), (1,2,1), (1,4,2), (1,6,1),
              (2,2,0), (2,4,1),
              (3,3,0), (3,5,1),
              (4,4,0),
              (5,5,0),
              (6,6,0);

 --rollback drop table comment cascade ; drop table user_ cascade; drop table comment_tree cascade;


