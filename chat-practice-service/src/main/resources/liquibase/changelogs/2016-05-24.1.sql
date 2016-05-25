--liquibase formatted sql
--changeset tyler:2


-- create children_view, basically id, and # of children
create view children_view as
select p.parent_id, count(*)-1 as num_of_children
from comment_tree as p
--where p.parent_id = 1
group by p.parent_id
order by p.parent_id asc;


create view comment_breadcrumbs_view as
select d.id,
       concat(repeat('-', p.path_length), d.text_) as hier,
       p.path_length, p.parent_id, p.child_id,
       array_agg(crumbs.parent_id) as breadcrumbs,
       count(crumbs.parent_id)-1 as num_of_parents,
       cv.num_of_children
from comment as d
join comment_tree as p on d.id = p.child_id
join comment_tree as crumbs on crumbs.child_id = p.child_id
join children_view as cv on d.id = cv.parent_id
where p.parent_id = 1
group by d.id, p.path_length, p.parent_id, p.child_id, cv.num_of_children
order by breadcrumbs;

select * from comment_breadcrumbs_view where parent_id = 1;


-- Select all top level parents
create view comment_threaded_view as
select b.* from comment_breadcrumbs_view as a
join comment_breadcrumbs_view as b
on a.id = b.parent_id
and a.num_of_parents = 0
order by a.id, b.breadcrumbs;




--rollback drop view comment_threaded_view, comment_breadcrumbs_view; drop view children_view;