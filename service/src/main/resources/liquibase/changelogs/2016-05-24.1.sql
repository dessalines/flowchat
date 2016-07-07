--liquibase formatted sql
--changeset tyler:2

-- create the user view
create view user_view as
select u.id,
    u.name,
    fu.id as full_user_id,
    fu.email
from user_ as u
join full_user as fu on u.id = fu.user_id;

create view user_login_view as
select u.id,
    u.name,
    fu.email,
    fu.id as full_user_id,
    l.id as login_id,
    l.auth,
    l.expire_time,
    u.created
from user_ as u
left join full_user as fu on fu.user_id = u.id
join login as l on l.user_id = u.id;

create view discussion_full_view as
select d.id,
    d.user_id,
    u.name as user_name,
    d.title,
    d.link,
    d.text_,
    d.private,
    d.deleted,
    avg(dr.rank) as avg_rank,
    count(distinct dr.id) as number_of_votes,
    array_agg(t.id order by t.id asc) as tag_ids,
    array_agg(t.name order by t.id asc) as tag_names,
    array_agg(u2.id order by u2.id asc) as private_user_ids,
    array_agg(u2.name order by u2.id asc) as private_user_names,
    d.created,
    d.modified
from discussion as d
left join discussion_rank as dr on dr.discussion_id = d.id
left join discussion_tag as dt on dt.discussion_id = d.id
left join tag as t on dt.tag_id = t.id
left join user_ as u on d.user_id = u.id
left join private_discussion_user as pdu on pdu.discussion_id = d.id
left join user_ as u2 on pdu.user_id = u2.id
group by d.id, d.user_id, u.name, d.title, d.link, d.text_, d.private, dt.discussion_id, dr.discussion_id
order by d.id;
--

create view discussion_notext_view as
select id,
    user_id,
    user_name,
    title,
    link,
    private,
    deleted,
    avg_rank,
    number_of_votes,
    tag_ids,
    tag_names,
    private_user_ids,
    private_user_names,
    created,
    modified
from discussion_full_view;


-- TODO
-- create table discussion_notext_view as


-- create children_view, basically id, and # of children
create view children_view as
select p.parent_id, count(*)-1 as num_of_children
from comment_tree as p
--where p.parent_id = 1
group by p.parent_id
order by p.parent_id asc;


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

--select * from comment_breadcrumbs_view where parent_id = 1;


-- Select all top level parents
create view comment_threaded_view as
select b.*
from comment_breadcrumbs_view as a
join comment_breadcrumbs_view as b
on a.id = b.parent_id
and a.num_of_parents = 0
order by a.id, b.breadcrumbs;

--select * from comment_threaded_view where discussion_id = 1;




--rollback drop view user_view cascade; drop view user_login_view cascade; drop view comment_threaded_view cascade; drop view comment_breadcrumbs_view cascade; drop view children_view cascade; drop view discussion_full_view cascade; drop view discussion_notext_view cascade;
