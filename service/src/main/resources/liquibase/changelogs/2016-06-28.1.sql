--liquibase formatted sql
--changeset tyler:4

create view popular_tags_view as
select t.*,
count(dt.tag_id)
from tag as t
left join discussion_tag as dt on dt.tag_id = t.id
group by t.id;

--rollback drop view popular_tags_view cascade;

