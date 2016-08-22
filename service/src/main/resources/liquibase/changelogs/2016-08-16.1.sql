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
    values ('Creator'),('Moderator'),('User'),('Blocked');

--rollback drop table community_role cascade;

create table community_tag (
    id bigserial primary key,
    community_id bigint not null,
    tag_id bigint not null,
    created timestamp default current_timestamp,
    constraint fk1_community_tag_community foreign key (community_id)
        references community (id)
        on update cascade on delete cascade,
    constraint fk2_community_tag_tag foreign key (tag_id)
        references tag (id)
        on update cascade on delete cascade,
    constraint fk3_community_tag_unique_1 unique(community_id, tag_id)
);

--rollback drop table community_tag cascade;

create table community_rank (
    id bigserial primary key,
    community_id bigint not null,
    user_id bigint not null,
    rank smallint,
    created timestamp default current_timestamp,
    constraint fk1_community_rank_community foreign key (community_id)
        references community (id)
        on update cascade on delete cascade,
    constraint fk2_community_rank_user foreign key (user_id)
        references user_ (id)
        on update cascade on delete cascade,
    constraint fk3_community_rank_unique_1 unique (community_id, user_id)
);

--rollback drop table community_rank cascade;


create table community_user (
    id bigserial primary key,
    user_id bigint not null,
    community_id bigint not null,
    community_role_id bigint not null,
    created timestamp default current_timestamp,
    constraint fk1_community_user_user foreign key (user_id)
        references user_ (id)
        on update cascade on delete cascade,
    constraint fk2_community_user_community foreign key (community_id)
        references community (id)
        on update cascade on delete cascade,
    constraint fk3_community_user_community_role foreign key (community_role_id)
        references community_role (id)
        on update cascade on delete cascade,
    constraint fk4_community_user_unique_1 unique(user_id, community_id)
);

--rollback drop table community_user cascade;


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

create table discussion_user (
    id bigserial primary key,
    user_id bigint not null,
    discussion_id bigint not null,
    discussion_role_id bigint not null,
    created timestamp default current_timestamp,
    constraint fk1_discussion_user_user foreign key (user_id)
        references user_ (id)
        on update cascade on delete cascade,
    constraint fk2_discussion_user_discussion foreign key (discussion_id)
        references discussion (id)
        on update cascade on delete cascade,
    constraint fk3_discussion_user_discussion_role foreign key (discussion_role_id)
        references discussion_role (id)
        on update cascade on delete cascade,
    constraint fk4_discussion_user_unique_1 unique(user_id, discussion_id)
);

-- copying the old data into the new tables

insert into discussion_user(user_id, discussion_id, discussion_role_id)
    select user_id, id as discussion_id, 1 as discussion_role_id
    from discussion;

insert into discussion_user(user_id, discussion_id, discussion_role_id)
    select private_discussion_user.user_id, private_discussion_user.discussion_id, 2 as discussion_role_id
    from private_discussion_user
    inner join discussion
    on private_discussion_user.discussion_id = discussion.id
    where discussion.user_id != private_discussion_user.user_id;

insert into discussion_user(user_id, discussion_id, discussion_role_id)
    select user_id, discussion_id, 3 as discussion_role_id
    from blocked_discussion_user;


--rollback drop table discussion_user cascade;

-- Adding a default community called vanilla
insert into community (name, text_) values ('vanilla', 'Flowchat''s default community.');
insert into community_user (community_id, user_id, community_role_id) values (1, 1, 1);

alter table discussion add column community_id bigint not null default 1;
alter table discussion add constraint fk4_discussion_community foreign key (community_id)
        references community (id)
        on update cascade on delete cascade;

--rollback alter table discussion drop column community_id cascade;


-- Removing the column from discussion
-- A backup table for the old discussion data
select * into deprecated_discussion from discussion;
alter table discussion drop column user_id cascade;
--rollback alter table discussion add column user_id bigint not null default 1;
--rollback update discussion set user_id = deprecated_discussion.user_id from deprecated_discussion where discussion.id = deprecated_discussion.id;
--rollback drop table deprecated_discussion;

-- backup the private and blocked tables(using the discussion_role table now)
select * into deprecated_private_discussion_user from private_discussion_user;
select * into deprecated_blocked_discussion_user from blocked_discussion_user;
drop table private_discussion_user;
drop table blocked_discussion_user;
--rollback create table private_discussion_user (     id bigserial primary key,     discussion_id bigint not null,     user_id bigint not null,     created timestamp default current_timestamp,     constraint fk1_private_discussion_discussion_user foreign key (discussion_id)         references discussion (id)         on update cascade on delete cascade,     constraint fk2_private_discussion_user foreign key (user_id)         references user_ (id)         on update cascade on delete cascade,     constraint fk3_private_discussion_unique_1 unique(discussion_id, user_id) );
--rollback create table blocked_discussion_user (     id bigserial primary key,     discussion_id bigint not null,     user_id bigint not null,     created timestamp default current_timestamp,     constraint fk1_blocked_discussion_discussion_user foreign key (discussion_id)         references discussion (id)         on update cascade on delete cascade,     constraint fk2_blocked_discussion_user foreign key (user_id)         references user_ (id)         on update cascade on delete cascade,     constraint fk3_blocked_discussion_unique_1 unique(discussion_id, user_id) );
--rollback insert into private_discussion_user select * from deprecated_private_discussion_user;
--rollback insert into blocked_discussion_user select * from deprecated_blocked_discussion_user;
--rollback drop table deprecated_private_discussion_user;
--rollback drop table deprecated_blocked_discussion_user;


create view discussion_full_view as
select d.id,
    d.community_id,
    d.title,
    d.link,
    d.text_,
    d.private,
    d.deleted,
    avg(dr.rank) as avg_rank,
    count(distinct dr.id) as number_of_votes,
    array_agg(dt.tag_id) as tag_ids,
    d.created,
    d.modified
from discussion as d
left join discussion_tag as dt on dt.discussion_id = d.id
left join discussion_rank as dr on dr.discussion_id = d.id
group by d.id, d.title, d.link, d.text_, d.private, d.deleted, d.created, d.modified, dt.discussion_id
order by d.id;

-- The verbose old view
--rollback create view discussion_full_view as select d.id, d.user_id, u.name as user_name, d.title, d.link, d.text_, d.private, d.deleted, avg(dr.rank) as avg_rank, count(distinct dr.id) as number_of_votes, array_agg(t.id order by t.id asc) as tag_ids, array_agg(t.name order by t.id asc) as tag_names, array_agg(u2.id order by u2.id asc) as private_user_ids, array_agg(u2.name order by u2.id asc) as private_user_names, array_agg(u3.id order by u3.id asc) as blocked_user_ids, array_agg(u3.name order by u3.id asc) as blocked_user_names, d.created, d.modified from discussion as d left join discussion_rank as dr on dr.discussion_id = d.id left join discussion_tag as dt on dt.discussion_id = d.id left join tag as t on dt.tag_id = t.id left join user_ as u on d.user_id = u.id left join private_discussion_user as pdu on pdu.discussion_id = d.id left join user_ as u2 on pdu.user_id = u2.id left join blocked_discussion_user as bdu on bdu.discussion_id = d.id left join user_ as u3 on bdu.user_id = u3.id group by d.id, d.user_id, u.name, d.title, d.link, d.text_, d.private, dt.discussion_id, dr.discussion_id order by d.id;

create view discussion_notext_view as
select id,
    community_id,
    title,
    link,
    private,
    deleted,
    avg_rank,
    number_of_votes,
    tag_ids,
    created,
    modified
from discussion_full_view;

--rollback create view discussion_notext_view as select id, user_id, user_name, title, link, private, deleted, avg_rank, number_of_votes, tag_ids, tag_names, private_user_ids, private_user_names, blocked_user_ids, blocked_user_names, created, modified from discussion_full_view;

create view comment_intermediate_view as
select d.id,
--       concat(repeat('-', p.path_length), d.text_) as hier,
       d.user_id,
       u.name as user_name,
       d.discussion_id,
       d.text_,
       p.path_length, p.parent_id, p.child_id,
       array_agg(crumbs.parent_id order by crumbs.parent_id) as breadcrumbs,
       count(crumbs.parent_id)-1 as num_of_parents,
       cv.num_of_children,
--       avg(cr.rank) as avg_rank,
       d.deleted,
       d.read,
       d.created,
       d.modified
from comment as d
join comment_tree as p on d.id = p.child_id
join comment_tree as crumbs on crumbs.child_id = p.child_id
join children_view as cv on d.id = cv.parent_id
join user_ as u on d.user_id = u.id
--where p.parent_id = 1
group by d.id, p.path_length, p.parent_id, p.child_id, cv.num_of_children, u.name
order by breadcrumbs;

--rollback drop view comment_intermediate_view cascade;
--rollback create view comment_intermediate_view as select d.id, d.user_id, u.name as user_name, d.discussion_id, d2.user_id as discussion_owner_id, d.text_, p.path_length, p.parent_id, p.child_id, array_agg(crumbs.parent_id order by crumbs.parent_id) as breadcrumbs, count(crumbs.parent_id)-1 as num_of_parents, cv.num_of_children, d.deleted, d.read, d.created, d.modified from comment as d join comment_tree as p on d.id = p.child_id join comment_tree as crumbs on crumbs.child_id = p.child_id join children_view as cv on d.id = cv.parent_id join user_ as u on d.user_id = u.id join discussion as d2 on d2.id = d.discussion_id group by d.id, p.path_length, p.parent_id, p.child_id, cv.num_of_children, u.name, d2.user_id order by breadcrumbs;

-- Necessary to join to the rank, couldn't get it working with the complicated intermediary above
create view comment_breadcrumbs_view as
select d.*,
d2.user_id as parent_user_id,
avg(cr.rank) as avg_rank,
count(cr.rank) as number_of_votes
from comment_intermediate_view as d
join comment as d2 on d.parent_id = d2.id
left join comment_rank as cr on d.id = cr.comment_id
group by d.id, d2.user_id, d.user_id, d.user_name, d.discussion_id, d.text_,
                d.path_length, d.parent_id, d.child_id,
                d.breadcrumbs,
                d.num_of_parents,
                d.num_of_children,
                d.deleted,
                d.read,
                d.created,
                d.modified
order by d.breadcrumbs;

--rollback create view comment_breadcrumbs_view as select d.*, d2.user_id as parent_user_id, avg(cr.rank) as avg_rank, count(cr.rank) as number_of_votes from comment_intermediate_view as d join comment as d2 on d.parent_id = d2.id left join comment_rank as cr on d.id = cr.comment_id group by d.id, d2.user_id, d.user_id, d.user_name, d.discussion_id, d.discussion_owner_id, d.text_, d.path_length, d.parent_id, d.child_id, d.breadcrumbs, d.num_of_parents, d.num_of_children, d.deleted, d.read, d.created, d.modified order by d.breadcrumbs;



-- Select all top level parents
create view comment_threaded_view as
select b.*
from comment_breadcrumbs_view as a
join comment_breadcrumbs_view as b
on a.id = b.parent_id
and a.num_of_parents = 0
order by a.id, b.breadcrumbs;

--rollback create view comment_threaded_view as select b.* from comment_breadcrumbs_view as a join comment_breadcrumbs_view as b on a.id = b.parent_id and a.num_of_parents = 0 order by a.id, b.breadcrumbs;

-- Adding the new views
create view discussion_tag_view as
select dt.discussion_id, dt.tag_id, t.name
from discussion_tag as dt
inner join discussion as d on d.id = dt.discussion_id
inner join tag as t on t.id = dt.tag_id;

--rollback drop view discussion_tag_view;

create view discussion_user_view as
select ud.user_id, ud.discussion_id, u.name, ud.discussion_role_id, d.deleted
from discussion_user as ud
inner join discussion as d on d.id = ud.discussion_id
inner join user_ as u on u.id = ud.user_id;

create view community_user_view as
select uc.user_id, uc.community_id, u.name, uc.community_role_id, c.deleted
from community_user as uc
inner join community as c on c.id = uc.community_id
inner join user_ as u on u.id = uc.user_id;

create view favorite_discussion_user_view as
select ud.user_id, ud.discussion_id, u.name, d.deleted
from favorite_discussion_user as ud
inner join discussion as d on d.id = ud.discussion_id
inner join user_ as u on u.id = ud.user_id;

--rollback drop view favorite_discussion_user_view;

create view community_view as
select c.id,
    c.name,
    c.text_,
    c.private,
    c.deleted,
    avg(cr.rank) as avg_rank,
    count(distinct cr.id) as number_of_votes,
    array_agg(ct.tag_id) as tag_ids,
    c.created,
    c.modified
from community as c
left join community_tag as ct on ct.community_id = c.id
left join community_rank as cr on cr.community_id = c.id
group by c.id, c.name, c.text_, c.private, c.deleted, c.created, c.modified
order by c.id;

create view community_notext_view as
select id,
    name,
    private,
    deleted,
    avg_rank,
    number_of_votes,
    tag_ids,
    created,
    modified
from community_view;

create view community_tag_view as
select dt.community_id, dt.tag_id, t.name
from community_tag as dt
inner join community as d on d.id = dt.community_id
inner join tag as t on t.id = dt.tag_id;













