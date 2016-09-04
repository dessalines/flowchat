--liquibase formatted sql
--changeset tyler:10

-- Add a modified_by_user_id column to comment, discussion, community
alter table discussion add column modified_by_user_id bigint not null default 1;
update discussion set modified_by_user_id = discussion_user.user_id from discussion_user where discussion_user.discussion_id = discussion.id and discussion_role_id = 1;

alter table discussion add constraint fk1_discussion_modified_by_user foreign key (modified_by_user_id)
        references user_ (id)
        on update cascade on delete cascade;

--rollback alter table discussion drop constraint fk1_discussion_modified_by_user;
--rollback alter table discussion drop column modified_by_user_id cascade;

alter table comment add column modified_by_user_id bigint not null default 1;
update comment set modified_by_user_id = comment.user_id;

alter table comment add constraint fk3_comment_modified_by_user foreign key (modified_by_user_id)
        references user_ (id)
        on update cascade on delete cascade;

--rollback alter table comment drop constraint fk3_comment_modified_by_user;
--rollback alter table comment drop column modified_by_user_id cascade;

alter table community add column modified_by_user_id bigint not null default 1;
update community set modified_by_user_id = community_user.user_id from community_user where community_user.community_id = community.id and community_role_id = 1;

alter table community add constraint fk3_community_modified_by_user foreign key (modified_by_user_id)
        references user_ (id)
        on update cascade on delete cascade;

--rollback alter table community drop constraint fk3_community_modified_by_user;
--rollback alter table community drop column modified_by_user_id cascade;


-- alter the views, and the audit view to get the modifier/deleter

drop view community_audit_view;

create view community_audit_view as
select audit.logged_actions.*,
d.id as discussion_id,
d.title as discussion_title,
d.community_id,
0 as role_id,
c.user_id,
u1.name as user_name,
c.modified_by_user_id,
u2.name as modified_by_user_name
from audit.logged_actions
left join comment as c on c.id = audit.logged_actions.id
left join discussion as d on c.discussion_id = d.id
left join user_ as u1 on c.user_id = u1.id
left join user_ as u2 on c.modified_by_user_id = u2.id
where audit.logged_actions.table_name = 'comment'
union
select audit.logged_actions.*,
d.id as discussion_id,
d.title as discussion_title,
d.community_id,
0 as role_id,
null as user_id,
null as user_name,
d.modified_by_user_id,
u.name as modified_by_user_name
from audit.logged_actions
left join discussion as d on d.id = audit.logged_actions.id
left join user_ as u on d.modified_by_user_id = u.id
where audit.logged_actions.table_name = 'discussion'
union
select audit.logged_actions.*,
null as discussion_id,
null as discussion_title,
cast(split_part(coalesce(original_data, new_data), ',', 3) as bigint) as community_id,
cast(split_part(coalesce(original_data, new_data), ',', 4) as bigint) as role_id,
cast(split_part(coalesce(original_data, new_data), ',', 2) as bigint) as user_id,
u1.name as user_name,
null as modified_by_user_id,
null as modified_by_user_name
from audit.logged_actions
left join community_user as cu on cu.id = audit.logged_actions.id
left join user_ as u1 on cast(split_part(coalesce(original_data, new_data), ',', 2) as bigint) = u1.id
where audit.logged_actions.table_name = 'community_user'
order by action_tstamp desc;

--rollback create view community_audit_view as select audit.logged_actions.*, discussion.id as discussion_id, discussion.community_id, comment.user_id, user_.name as user_name from audit.logged_actions inner join comment on comment.id = audit.logged_actions.id inner join discussion on comment.discussion_id = discussion.id inner join user_ on comment.user_id = user_.id where audit.logged_actions.table_name = 'comment' union select audit.logged_actions.*, discussion.id as discussion_id, discussion.community_id, null as user_id, null as user_name from audit.logged_actions inner join discussion on discussion.id = audit.logged_actions.id where audit.logged_actions.table_name = 'discussion' union select audit.logged_actions.*, null as discussion_id, community_user.community_id, community_user.user_id, user_.name as user_name from audit.logged_actions inner join community_user on community_user.id = audit.logged_actions.id inner join user_ on community_user.user_id = user_.id where audit.logged_actions.table_name = 'community_user' order by action_tstamp desc;

-- Clear out the audit logging table
delete from audit.logged_actions;


drop view discussion_full_view cascade;

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
    d.modified_by_user_id,
    u.name as modified_by_user_name,
    d.created,
    d.modified
from discussion as d
left join discussion_tag as dt on dt.discussion_id = d.id
left join discussion_rank as dr on dr.discussion_id = d.id
left join user_ as u on u.id = d.modified_by_user_id
group by d.id, d.title, d.link, d.text_, d.private, d.deleted, d.created, d.modified, dt.discussion_id, u.name
order by d.id;

--rollback create view discussion_full_view as select d.id,     d.community_id,     d.title,     d.link,     d.text_,     d.private,     d.deleted,     avg(dr.rank) as avg_rank,     count(distinct dr.id) as number_of_votes,     array_agg(dt.tag_id) as tag_ids,     d.created,     d.modified from discussion as d left join discussion_tag as dt on dt.discussion_id = d.id left join discussion_rank as dr on dr.discussion_id = d.id group by d.id, d.title, d.link, d.text_, d.private, d.deleted, d.created, d.modified, dt.discussion_id order by d.id;

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
    modified_by_user_id,
    modified_by_user_name,
    created,
    modified
from discussion_full_view;

--rollback create view discussion_notext_view as select id,     community_id,     title,     link,     private,     deleted,     avg_rank,     number_of_votes,     tag_ids,     created,     modified from discussion_full_view;

drop view comment_intermediate_view cascade;

create view comment_intermediate_view as
select d.id,
       d.user_id,
       u.name as user_name,
       d.modified_by_user_id,
       u2.name as modified_by_user_name,
       d.discussion_id,
       d.text_,
       p.path_length, p.parent_id, p.child_id,
       array_agg(crumbs.parent_id order by crumbs.parent_id) as breadcrumbs,
       count(crumbs.parent_id)-1 as num_of_parents,
       cv.num_of_children,
       d.deleted,
       d.read,
       d.created,
       d.modified
from comment as d
join comment_tree as p on d.id = p.child_id
join comment_tree as crumbs on crumbs.child_id = p.child_id
join children_view as cv on d.id = cv.parent_id
join user_ as u on d.user_id = u.id
join user_ as u2 on d.modified_by_user_id = u2.id
group by d.id, p.path_length, p.parent_id, p.child_id, cv.num_of_children, u.name, u2.name
order by breadcrumbs;

--rollback create view comment_intermediate_view as select d.id,        d.user_id,        u.name as user_name,        d.discussion_id,        d.text_,        p.path_length, p.parent_id, p.child_id,        array_agg(crumbs.parent_id order by crumbs.parent_id) as breadcrumbs,        count(crumbs.parent_id)-1 as num_of_parents,        cv.num_of_children,        d.deleted,        d.read,        d.created,        d.modified from comment as d join comment_tree as p on d.id = p.child_id join comment_tree as crumbs on crumbs.child_id = p.child_id join children_view as cv on d.id = cv.parent_id join user_ as u on d.user_id = u.id group by d.id, p.path_length, p.parent_id, p.child_id, cv.num_of_children, u.name order by breadcrumbs;

create view comment_breadcrumbs_view as
select d.*,
d2.user_id as parent_user_id,
avg(cr.rank) as avg_rank,
count(cr.rank) as number_of_votes
from comment_intermediate_view as d
join comment as d2 on d.parent_id = d2.id
left join comment_rank as cr on d.id = cr.comment_id
group by d.id, d2.user_id, d.user_id, d.user_name, d.modified_by_user_id, d.modified_by_user_name, d.discussion_id, d.text_,
                d.path_length, d.parent_id, d.child_id,
                d.breadcrumbs,
                d.num_of_parents,
                d.num_of_children,
                d.deleted,
                d.read,
                d.created,
                d.modified
order by d.breadcrumbs;

--rollback create view comment_breadcrumbs_view as select d.*, d2.user_id as parent_user_id, avg(cr.rank) as avg_rank, count(cr.rank) as number_of_votes from comment_intermediate_view as d join comment as d2 on d.parent_id = d2.id left join comment_rank as cr on d.id = cr.comment_id group by d.id, d2.user_id, d.user_id, d.user_name, d.discussion_id, d.text_,                 d.path_length, d.parent_id, d.child_id,                 d.breadcrumbs,                 d.num_of_parents,                 d.num_of_children,                 d.deleted,                 d.read,                 d.created,                 d.modified order by d.breadcrumbs;

create view comment_threaded_view as
select b.*
from comment_breadcrumbs_view as a
join comment_breadcrumbs_view as b
on a.id = b.parent_id
and a.num_of_parents = 0
order by a.id, b.breadcrumbs;

--rollback create view comment_threaded_view as select b.* from comment_breadcrumbs_view as a join comment_breadcrumbs_view as b on a.id = b.parent_id and a.num_of_parents = 0 order by a.id, b.breadcrumbs;






