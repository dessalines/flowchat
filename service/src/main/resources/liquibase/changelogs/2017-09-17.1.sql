--liquibase formatted sql
--changeset tyler:15

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
    count(distinct c.id) as number_of_comments,
    array_agg(dt.tag_id) as tag_ids,
    d.modified_by_user_id,
    u.name as modified_by_user_name,
    d.created,
    d.modified
from discussion as d
left join discussion_tag as dt on dt.discussion_id = d.id
left join discussion_rank as dr on dr.discussion_id = d.id
left join user_ as u on u.id = d.modified_by_user_id
left join comment as c on c.discussion_id = d.id
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
    number_of_comments,
    tag_ids,
    modified_by_user_id,
    modified_by_user_name,
    created,
    modified
from discussion_full_view;

--rollback create view discussion_notext_view as select id,     community_id,     title,     link,     private,     deleted,     avg_rank,     number_of_votes,     tag_ids,     created,     modified from discussion_full_view;
