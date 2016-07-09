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
    constraint fk1_full_user_user foreign key (user_id)
        references user_ (id)
        on update cascade on delete cascade
);

CREATE TABLE login (
    id bigserial primary key,
    user_id bigint not null,
    auth VARCHAR(255) not null,
    expire_time timestamp default current_timestamp,
    created timestamp default current_timestamp,
    constraint fk1_login_user foreign key (user_id)
        references user_ (id)
        on update cascade on delete cascade
);

create table discussion (
    id bigserial primary key,
    user_id bigint not null,
    title varchar(140) not null,
    link varchar(255),
    text_ text,
    private boolean not null default false,
    deleted boolean not null default false,
    created timestamp default current_timestamp,
    modified timestamp
);

create table discussion_rank (
    id bigserial primary key,
    discussion_id bigint not null,
    user_id bigint not null,
    rank smallint,
    created timestamp default current_timestamp,
    constraint fk1_discussion_rank_discussion foreign key (discussion_id)
        references discussion (id)
        on update cascade on delete cascade,
    constraint fk2_discussion_rank_user foreign key (user_id)
        references user_ (id)
        on update cascade on delete cascade,
    constraint fk3_discussion_rank_unique_1 unique (discussion_id, user_id)
);

create table tag (
    id bigserial primary key,
    name varchar(50) not null unique,
    created timestamp default current_timestamp
);

create table discussion_tag (
    id bigserial primary key,
    discussion_id bigint not null,
    tag_id bigint not null,
    created timestamp default current_timestamp,
    constraint fk1_discussion_tag_discussion foreign key (discussion_id)
        references discussion (id)
        on update cascade on delete cascade,
    constraint fk2_discussion_tag_tag foreign key (tag_id)
        references tag (id)
        on update cascade on delete cascade,
    constraint fk3_discussion_tag_unique_1 unique(discussion_id, tag_id)
);

create table private_discussion_user (
    id bigserial primary key,
    discussion_id bigint not null,
    user_id bigint not null,
    created timestamp default current_timestamp,
    constraint fk1_private_discussion_user_discussion foreign key (discussion_id)
        references discussion (id)
        on update cascade on delete cascade,
    constraint fk2_private_discussion_user foreign key (user_id)
        references user_ (id)
        on update cascade on delete cascade,
    constraint fk3_private_discussion_unique_1 unique(discussion_id, user_id)
);

create table blocked_discussion_user (
    id bigserial primary key,
    discussion_id bigint not null,
    user_id bigint not null,
    created timestamp default current_timestamp,
    constraint fk1_blocked_discussion_user_discussion foreign key (discussion_id)
        references discussion (id)
        on update cascade on delete cascade,
    constraint fk2_blocked_discussion_user foreign key (user_id)
        references user_ (id)
        on update cascade on delete cascade,
    constraint fk3_blocked_discussion_unique_1 unique(discussion_id, user_id)
);

create table favorite_discussion_user (
    id bigserial primary key,
    discussion_id bigint not null,
    user_id bigint not null,
    created timestamp default current_timestamp,
    constraint fk1_favorite_discussion_user_discussion foreign key (discussion_id)
        references discussion (id)
        on update cascade on delete cascade,
    constraint fk2_favorite_discussion_user_user foreign key (user_id)
        references user_ (id)
        on update cascade on delete cascade,
    constraint fk3_favorite_discussion_user_unique_1 unique(discussion_id, user_id)
);


create table comment (
    id bigserial primary key,
    user_id bigint not null,
    discussion_id bigint not null,
    text_ text not null,
    deleted boolean not null default false,
    read boolean not null default false,
    created timestamp default current_timestamp,
    modified timestamp,
    constraint fk1_comment_user foreign key (user_id)
        references user_ (id)
        on update cascade on delete cascade,
    constraint fk2_comment_discussion foreign key (discussion_id)
        references discussion (id)
        on update cascade on delete cascade
);

create table comment_tree (
    id bigserial primary key,
    parent_id bigint not null,
    child_id bigint not null,
    path_length integer not null,
    created timestamp default current_timestamp,
    constraint fk1_comment_tree_parent_id foreign key (parent_id)
        references comment (id)
        on update cascade on delete cascade,
    constraint fk2_comment_tree_child_id foreign key (child_id)
        references comment (id)
        on update cascade on delete cascade
);

create table comment_rank (
    id bigserial primary key,
    comment_id bigint not null,
    user_id bigint not null,
    rank smallint,
    created timestamp default current_timestamp,
    constraint fk1_comment_rank_comment foreign key (comment_id)
        references comment (id)
        on update cascade on delete cascade,
    constraint fk2_comment_rank_user foreign key (user_id)
        references user_ (id)
        on update cascade on delete cascade,
    constraint fk3_comment_rank_unique_1 unique (comment_id, user_id)
);

create table ranking_constants (
    id bigserial primary key,
    created_weight numeric(20,4),
    number_of_votes_weight numeric(12,4),
    avg_rank_weight numeric(12,4)
);

insert into user_ (name)
    values ('user_1'),('user_2'),('user_3'), ('cardinal');

insert into full_user (user_id, email, password_encrypted)
    values (4, null, '9c71bkpBllep7HRvVhFO9IZkirBCmDQS');


insert into discussion (title, user_id, link, text_, private)
    values ('Power to the People?', 1, 'https://grist.files.wordpress.com/2016/03/black-panthers.jpg?w=1200&h=675&crop=1', null, false),
    ('Dogs', 2, null,'**ima** dog', false),
    ('Only kittens',3,  null, 'kittens only here plz.', true),
    ('Kittens', 1, 'http://google.com', null, false),
    ('Bees are cool, no?', 1, 'http://google.com', null, false),
    ('Should I go there?', 1, 'http://google.com', null, false),
    ('FULLCOMMUNISM FTW', 1, 'http://google.com', null, false),
    ('Love weed, hate poor people', 1, 'http://google.com', null, false),
    ('Puppies', 1, 'http://google.com', null, false),
    ('Mittens', 1, 'http://google.com', null, false);

insert into private_discussion_user(discussion_id, user_id)
    values (3,4);

insert into blocked_discussion_user(discussion_id, user_id)
    values(4,4);

insert into favorite_discussion_user(discussion_id, user_id)
    values (1,4),(3,4);

insert into discussion_rank (discussion_id, user_id, rank)
    values (1,1,21),(1,3,81),(2,2,87);

insert into tag (name)
    values ('animals'),('things_like'),('feathers');

insert into discussion_tag (discussion_id, tag_id)
    values (1,1),(1,2),(3,3);

insert into comment (text_, user_id, discussion_id)
	values ('Node 1',1,1),('Node 1.1',2,1),('Node 2',3,1),('Node 1.1.1',2,1),('Node 2.1',1,1),
	('Node 1.2',2,1),('Node 3',4,1),('Node 3.1',2,1);

insert into comment_rank (comment_id, user_id, rank)
    values (1, 2, 40), (1, 3, 75), (2, 1, 100);

insert into comment_tree (parent_id, child_id, path_length)
	values 	  (1,1,0), (1,2,1), (1,4,2), (1,6,1),
              (2,2,0), (2,4,1),
              (3,3,0), (3,5,1),
              (4,4,0),
              (5,5,0),
              (6,6,0),
              (7,7,0), (7,8,1),
              (8,8,0);

insert into ranking_constants (created_weight,number_of_votes_weight,avg_rank_weight)
    values (1000000, .001, .01);

--rollback drop table ranking_constants cascade; drop table login cascade; drop table full_user cascade; drop table comment cascade ; drop table user_ cascade; drop table comment_tree cascade; drop table discussion cascade; drop table comment_rank cascade; ;drop table discussion_tag cascade; drop table discussion_rank cascade; drop table private_discussion_user cascade;;drop table tag cascade; drop table favorite_discussion_user cascade;

