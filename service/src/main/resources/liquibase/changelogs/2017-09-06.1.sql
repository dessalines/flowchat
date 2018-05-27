--liquibase formatted sql
--changeset tyler:13

drop view discussion_full_view cascade;
drop view user_audit_view, community_audit_view;

alter table discussion alter column link type varchar(2000);

--rollback alter table discussion alter column link type varchar(255);

alter table discussion alter column title type varchar(255);

--rollback alter table discussion alter column title type varchar(140);

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


create view user_audit_view as
select audit.logged_actions.*,
c.text_ as comment_text,
d.id as discussion_id,
d.title as discussion_title,
d.community_id,
co.name as community_name,
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
left join community as co on d.community_id = co.id
where audit.logged_actions.table_name = 'comment'
union
-- This second union is because the comment_audit only records updates or deletes, and so check where modified=null from the comment table
select c.id,
null as schema_name,
'comment' as table_name,
null as p_user_name,
c.created as action_tstamp,
'I' as action,
null as original_data,
null as new_data,
null as query,
c.text_ as comment_text,
d.id as discussion_id,
d.title as discussion_title,
d.community_id,
co.name as community_name,
0 as role_id,
c.user_id,
u1.name as user_name,
c.modified_by_user_id,
u2.name as modified_by_user_name
from comment as c
left join discussion as d on c.discussion_id = d.id
left join user_ as u1 on c.user_id = u1.id
left join user_ as u2 on c.modified_by_user_id = u2.id
left join community as co on d.community_id = co.id
where c.modified is null
union
select audit.logged_actions.*,
null as comment_text,
null as discussion_id,
null as discussion_title,
cast(split_part(coalesce(original_data, new_data), ',', 3) as bigint) as community_id,
co.name as community_name,
cast(split_part(coalesce(original_data, new_data), ',', 4) as bigint) as role_id,
cast(split_part(coalesce(original_data, new_data), ',', 2) as bigint) as user_id,
u1.name as user_name,
null as modified_by_user_id,
null as modified_by_user_name
from audit.logged_actions
left join community_user as cu on cu.id = audit.logged_actions.id
left join user_ as u1 on cast(split_part(coalesce(original_data, new_data), ',', 2) as bigint) = u1.id
left join community as co on cast(split_part(coalesce(original_data, new_data), ',', 3) as bigint) = co.id
where audit.logged_actions.table_name = 'community_user'
union
select audit.logged_actions.*,
null as comment_text,
cast(split_part(coalesce(original_data, new_data), ',', 3) as bigint) as discussion_id,
d.title as discussion_title,
null as community_id,
null as community_name,
cast(split_part(coalesce(original_data, new_data), ',', 4) as bigint) as role_id,
cast(split_part(coalesce(original_data, new_data), ',', 2) as bigint) as user_id,
u1.name as user_name,
null as modified_by_user_id,
null as modified_by_user_name
from audit.logged_actions
left join discussion_user as du on du.id = audit.logged_actions.id
left join discussion as d on cast(split_part(coalesce(original_data, new_data), ',', 3) as bigint) = d.id
left join user_ as u1 on cast(split_part(coalesce(original_data, new_data), ',', 2) as bigint) = u1.id
where audit.logged_actions.table_name = 'discussion_user'
order by action_tstamp desc;


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


