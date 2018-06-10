--liquibase formatted sql
--changeset tyler:20

-- adding stickied discussions and comments

alter table discussion add column stickied boolean default false;

--rollback alter table discussion drop column stickied cascade;

drop view discussion_full_view cascade;

CREATE VIEW public.discussion_full_view AS
 SELECT d.id,
    d.community_id,
    d.title,
    d.link,
    d.text_,
    d.private,
    d.nsfw,
    d.stickied,
    d.deleted,
    avg(dr.rank) AS avg_rank,
    count(DISTINCT dr.id) AS number_of_votes,
    count(DISTINCT c.id) AS number_of_comments,
    array_agg(dt.tag_id) AS tag_ids,
    d.modified_by_user_id,
    u.name AS modified_by_user_name,
    d.created,
    d.modified
   FROM ((((public.discussion d
     LEFT JOIN public.discussion_tag dt ON ((dt.discussion_id = d.id)))
     LEFT JOIN public.discussion_rank dr ON ((dr.discussion_id = d.id)))
     LEFT JOIN public.user_ u ON ((u.id = d.modified_by_user_id)))
     LEFT JOIN public.comment c ON ((c.discussion_id = d.id)))
  GROUP BY d.id, d.title, d.link, d.text_, d.private, d.deleted, d.created, d.modified, dt.discussion_id, u.name
  ORDER BY d.id;

CREATE VIEW public.discussion_notext_view AS
 SELECT discussion_full_view.id,
    discussion_full_view.community_id,
    discussion_full_view.title,
    discussion_full_view.link,
    discussion_full_view.private,
    discussion_full_view.nsfw,
    discussion_full_view.stickied,
    discussion_full_view.deleted,
    discussion_full_view.avg_rank,
    discussion_full_view.number_of_votes,
    discussion_full_view.number_of_comments,
    discussion_full_view.tag_ids,
    discussion_full_view.modified_by_user_id,
    discussion_full_view.modified_by_user_name,
    discussion_full_view.created,
    discussion_full_view.modified
   FROM public.discussion_full_view;


--rollback drop view if exists discussion_full_view cascade;

--rollback CREATE VIEW public.discussion_full_view AS SELECT d.id, d.community_id, d.title, d.link, d.text_, d.private, d.nsfw, d.deleted, avg(dr.rank) AS avg_rank, count(DISTINCT dr.id) AS number_of_votes, count(DISTINCT c.id) AS number_of_comments, array_agg(dt.tag_id) AS tag_ids, d.modified_by_user_id, u.name AS modified_by_user_name, d.created, d.modified FROM ((((public.discussion d LEFT JOIN public.discussion_tag dt ON ((dt.discussion_id = d.id))) LEFT JOIN public.discussion_rank dr ON ((dr.discussion_id = d.id))) LEFT JOIN public.user_ u ON ((u.id = d.modified_by_user_id))) LEFT JOIN public.comment c ON ((c.discussion_id = d.id))) GROUP BY d.id, d.title, d.link, d.text_, d.private, d.deleted, d.created, d.modified, dt.discussion_id, u.name ORDER BY d.id;

--rollback CREATE VIEW public.discussion_notext_view AS SELECT discussion_full_view.id, discussion_full_view.community_id, discussion_full_view.title, discussion_full_view.link, discussion_full_view.private, discussion_full_view.nsfw, discussion_full_view.deleted, discussion_full_view.avg_rank, discussion_full_view.number_of_votes, discussion_full_view.number_of_comments, discussion_full_view.tag_ids, discussion_full_view.modified_by_user_id, discussion_full_view.modified_by_user_name, discussion_full_view.created, discussion_full_view.modified FROM public.discussion_full_view;

alter table comment add column stickied boolean default false;

--rollback alter table comment drop column stickied cascade;

drop view comment_intermediate_view cascade;
drop view if exists comment_breadcrumbs_view cascade;

CREATE VIEW public.comment_intermediate_view AS
 SELECT d.id,
    d.user_id,
    u.name AS user_name,
    d.modified_by_user_id,
    u2.name AS modified_by_user_name,
    d.discussion_id,
    d.text_,
    p.path_length,
    p.parent_id,
    p.child_id,
    array_agg(crumbs.parent_id ORDER BY crumbs.parent_id) AS breadcrumbs,
    (count(crumbs.parent_id) - 1) AS num_of_parents,
    cv.num_of_children,
    d.stickied,
    d.deleted,
    d.read,
    d.created,
    d.modified
   FROM (((((public.comment d
     JOIN public.comment_tree p ON ((d.id = p.child_id)))
     JOIN public.comment_tree crumbs ON ((crumbs.child_id = p.child_id)))
     JOIN public.children_view cv ON ((d.id = cv.parent_id)))
     JOIN public.user_ u ON ((d.user_id = u.id)))
     JOIN public.user_ u2 ON ((d.modified_by_user_id = u2.id)))
  GROUP BY d.id, p.path_length, p.parent_id, p.child_id, cv.num_of_children, u.name, u2.name
  ORDER BY (array_agg(crumbs.parent_id ORDER BY crumbs.parent_id));

CREATE VIEW public.comment_breadcrumbs_view AS
 SELECT d.id,
    d.user_id,
    d.user_name,
    d.modified_by_user_id,
    d.modified_by_user_name,
    d.discussion_id,
    d.text_,
    d.path_length,
    d.parent_id,
    d.child_id,
    d.breadcrumbs,
    d.num_of_parents,
    d.num_of_children,
    d.stickied,
    d.deleted,
    d.read,
    d.created,
    d.modified,
    d2.user_id AS parent_user_id,
    avg(cr.rank) AS avg_rank,
    count(cr.rank) AS number_of_votes
   FROM ((public.comment_intermediate_view d
     JOIN public.comment d2 ON ((d.parent_id = d2.id)))
     LEFT JOIN public.comment_rank cr ON ((d.id = cr.comment_id)))
  GROUP BY d.id, d2.user_id, d.user_id, d.user_name, d.modified_by_user_id, d.modified_by_user_name, d.discussion_id, d.text_, d.path_length, d.parent_id, d.child_id, d.breadcrumbs, d.num_of_parents, d.num_of_children, d.stickied, d.deleted, d.read, d.created, d.modified
  ORDER BY d.breadcrumbs;

create view comment_threaded_view as
select b.*
from comment_breadcrumbs_view as a
join comment_breadcrumbs_view as b
on a.id = b.parent_id
and a.num_of_parents = 0
order by a.id, b.breadcrumbs;

--rollback drop view if exists comment_intermediate_view cascade;
--rollback drop view if exists comment_breadcrumbs_view cascade;

--rollback CREATE VIEW public.comment_intermediate_view AS SELECT d.id, d.user_id, u.name AS user_name, d.modified_by_user_id, u2.name AS modified_by_user_name, d.discussion_id, d.text_, p.path_length, p.parent_id, p.child_id, array_agg(crumbs.parent_id ORDER BY crumbs.parent_id) AS breadcrumbs, (count(crumbs.parent_id) - 1) AS num_of_parents, cv.num_of_children, d.deleted, d.read, d.created, d.modified FROM (((((public.comment d JOIN public.comment_tree p ON ((d.id = p.child_id))) JOIN public.comment_tree crumbs ON ((crumbs.child_id = p.child_id))) JOIN public.children_view cv ON ((d.id = cv.parent_id))) JOIN public.user_ u ON ((d.user_id = u.id))) JOIN public.user_ u2 ON ((d.modified_by_user_id = u2.id))) GROUP BY d.id, p.path_length, p.parent_id, p.child_id, cv.num_of_children, u.name, u2.name ORDER BY (array_agg(crumbs.parent_id ORDER BY crumbs.parent_id));

--rollback CREATE VIEW public.comment_breadcrumbs_view AS SELECT d.id, d.user_id, d.user_name, d.modified_by_user_id, d.modified_by_user_name, d.discussion_id, d.text_, d.path_length, d.parent_id, d.child_id, d.breadcrumbs, d.num_of_parents, d.num_of_children, d.deleted, d.read, d.created, d.modified, d2.user_id AS parent_user_id, avg(cr.rank) AS avg_rank, count(cr.rank) AS number_of_votes FROM ((public.comment_intermediate_view d JOIN public.comment d2 ON ((d.parent_id = d2.id))) LEFT JOIN public.comment_rank cr ON ((d.id = cr.comment_id))) GROUP BY d.id, d2.user_id, d.user_id, d.user_name, d.modified_by_user_id, d.modified_by_user_name, d.discussion_id, d.text_, d.path_length, d.parent_id, d.child_id, d.breadcrumbs, d.num_of_parents, d.num_of_children, d.deleted, d.read, d.created, d.modified ORDER BY d.breadcrumbs;

--rollback create view comment_threaded_view as select b.* from comment_breadcrumbs_view as a join comment_breadcrumbs_view as b on a.id = b.parent_id and a.num_of_parents = 0 order by a.id, b.breadcrumbs;


