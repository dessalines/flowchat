--liquibase formatted sql
--changeset tyler:8


-- Community table declarations

-- TODO unique constraints

-- TODO defaults

-- TODO need some kind of history for deleted comments, blocking users, and deleting threads, the 3 mod abilities

-- TODO need ability to restore deleted comments, threads, and unblock users

-- TODO refactor private, blocked, and favorite into new discussion_user table,
-- rename those to deprecated, delete after it works correctly


create table community (
    id bigserial primary key,
    name varchar(140) not null,
    text_ text,
    private boolean not null default false,
    deleted boolean not null default false,
    created timestamp default current_timestamp,
    modified timestamp,
    constraint fk1_community unique(name)
);

--rollback drop table community cascade;

create table community_role (
    id bigserial primary key,
    role varchar(140) not null,
    created timestamp default current_timestamp,
    constraint fk1_community_role_unique_1 unique(role)
);


-- Create the community roles
insert into community_role (role)
    values ('Creator'),('Mod'),('User'),('Blocked');

--rollback drop table community_role cascade;

create table user_community (
    id bigserial primary key,
    user_id bigint not null,
    community_id bigint not null,
    community_role_id bigint not null,
    created timestamp default current_timestamp,
    constraint fk1_user_community_user foreign key (user_id)
        references user_ (id)
        on update cascade on delete cascade,
    constraint fk2_user_community_community foreign key (community_id)
        references community (id)
        on update cascade on delete cascade,
    constraint fk3_user_community_community_role foreign key (community_role_id)
        references community_role (id)
        on update cascade on delete cascade,
    constraint fk4_user_community_unique_1 unique(user_id, community_id)
);

--rollback drop table user_community cascade;

-- The delete/restore log tables

create table log_action (
    id bigserial primary key,
    action_ varchar(140) not null,
    constraint fk1_log_action_unique_1 unique(action_)
);

insert into log_action (action_)
    values ('Deleted'),('Restored'),('Blocked'),('Unblocked'),('Favorited'),('Unfavorited');

--rollback drop table log_action cascade;

create table comment_log (
    id bigserial primary key,
    user_id bigint not null,
    comment_id bigint not null,
    log_action_id bigint not null,
    created timestamp default current_timestamp,
    constraint fk1_comment_log_user foreign key (user_id)
        references user_ (id)
        on update cascade on delete cascade,
    constraint fk2_comment_log_comment foreign key (comment_id)
        references comment (id)
        on update cascade on delete cascade,
    constraint fk3_comment_log_log_action foreign key (log_action_id)
        references log_action (id)
        on update cascade on delete cascade
);

--rollback drop table comment_log;

create table discussion_log (
    id bigserial primary key,
    user_id bigint not null,
    discussion_id bigint not null,
    log_action_id bigint not null,
    created timestamp default current_timestamp,
    constraint fk1_discussion_log_user foreign key (user_id)
        references user_ (id)
        on update cascade on delete cascade,
    constraint fk2_discussion_log_discussion foreign key (discussion_id)
        references discussion (id)
        on update cascade on delete cascade,
    constraint fk3_discussion_log_log_action foreign key (log_action_id)
        references log_action (id)
        on update cascade on delete cascade
);

--rollback drop table discussion_log;

create table user_discussion_log (
    id bigserial primary key,
    user_id bigint not null,
    discussion_id bigint not null,
    target_user_id bigint not null,
    log_action_id bigint not null,
    created timestamp default current_timestamp,
    constraint fk1_user_discussion_log_user foreign key (user_id)
        references user_ (id)
        on update cascade on delete cascade,
    constraint fk2_user_discussion_log_discussion foreign key (discussion_id)
        references discussion (id)
        on update cascade on delete cascade,
    constraint fk3_user_discussion_log_target_user foreign key (target_user_id)
        references user_ (id)
        on update cascade on delete cascade,
    constraint fk4_user_discussion_log_log_action foreign key (log_action_id)
        references log_action (id)
        on update cascade on delete cascade
);

--rollback drop table user_discussion_log;

create table user_community_log (
    id bigserial primary key,
    user_id bigint not null,
    community_id bigint not null,
    target_user_id bigint not null,
    log_action_id bigint not null,
    created timestamp default current_timestamp,
    constraint fk1_user_community_log_user foreign key (user_id)
        references user_ (id)
        on update cascade on delete cascade,
    constraint fk2_user_community_log_community foreign key (community_id)
        references community (id)
        on update cascade on delete cascade,
    constraint fk3_user_community_log_target_user foreign key (target_user_id)
        references user_ (id)
        on update cascade on delete cascade,
    constraint fk4_user_community_log_log_action foreign key (log_action_id)
        references log_action (id)
        on update cascade on delete cascade
);

--rollback drop table user_community_log;

create table discussion_role (
    id bigserial primary key,
    role varchar(140) not null,
    created timestamp default current_timestamp,
    constraint fk1_discussion_role_unique_1 unique(role)
);

-- Create the discussion roles
insert into discussion_role (role)
    values ('Creator'),('User'),('Blocked');

--rollback drop table discussion_role cascade;

create table user_discussion (
    id bigserial primary key,
    user_id bigint not null,
    discussion_id bigint not null,
    discussion_role_id bigint not null,
    created timestamp default current_timestamp,
    constraint fk1_user_discussion_user foreign key (user_id)
        references user_ (id)
        on update cascade on delete cascade,
    constraint fk2_user_discussion_discussion foreign key (discussion_id)
        references discussion (id)
        on update cascade on delete cascade,
    constraint fk3_user_discussion_discussion_role foreign key (discussion_role_id)
        references discussion_role (id)
        on update cascade on delete cascade,
    constraint fk4_user_discussion_unique_1 unique(user_id, discussion_id)
);

-- copying the old data into the new tables

insert into user_discussion(user_id, discussion_id, discussion_role_id)
    select user_id, id as discussion_id, 1 as discussion_role_id
    from discussion;

insert into user_discussion(user_id, discussion_id, discussion_role_id)
    select private_discussion_user.user_id, private_discussion_user.discussion_id, 2 as discussion_role_id
    from private_discussion_user
    inner join discussion
    on private_discussion_user.discussion_id = discussion.id
    where discussion.user_id != private_discussion_user.user_id;

insert into user_discussion(user_id, discussion_id, discussion_role_id)
    select user_id, discussion_id, 3 as discussion_role_id
    from blocked_discussion_user;


--rollback drop table user_discussion cascade;


-- renaming the private and blocked tables(using the discussion_role table now)
alter table private_discussion_user rename to deprecated_private_discussion_user;
alter table blocked_discussion_user rename to deprecated_blocked_discussion_user;
alter table discussion rename to deprecated_discussion;

select * into discussion from deprecated_discussion;

alter table discussion drop column user_id;

--rollback drop table discussion cascade;
--rollback alter table deprecated_discussion rename to discussion;
--rollback alter table deprecated_private_discussion_user rename to private_discussion_user;
--rollback alter table deprecated_blocked_discussion_user rename to blocked_discussion_user;

alter view discussion_full_view rename to deprecated_discussion_full_view;

create view discussion_full_view as
select d.id,
    d.title,
    d.link,
    d.text_,
    d.private,
    d.deleted,
    avg(dr.rank) as avg_rank,
    count(distinct dr.id) as number_of_votes,
    d.created,
    d.modified
from discussion as d
left join discussion_rank as dr on dr.discussion_id = d.id
group by d.id, d.title, d.link, d.text_, d.private, d.deleted, d.created, d.modified
order by d.id;

--rollback alter view deprecated_discussion_full_view rename to discussion_full_view;

alter view discussion_notext_view rename to deprecated_discussion_notext_view;

create view discussion_notext_view as
select id,
    title,
    link,
    private,
    deleted,
    avg_rank,
    number_of_votes,
    created,
    modified
from discussion_full_view;

--rollback alter view deprecated_discussion_notext_view rename to discussion_notext_view;

create view discussion_tag_view as
select dt.discussion_id, dt.tag_id, t.name
from discussion_tag as dt
inner join discussion as d on d.id = dt.discussion_id
inner join tag as t on t.id = dt.tag_id;

create view user_discussion_view as
select ud.user_id, ud.discussion_id, u.name
from user_discussion as ud
inner join discussion as d on d.id = ud.discussion_id
inner join user_ as u on u.id = ud.user_id;

create view user_community_view as
select uc.user_id, uc.community_id, u.name
from user_community as uc
inner join discussion as d on d.id = uc.community_id
inner join user_ as u on u.id = uc.user_id;












