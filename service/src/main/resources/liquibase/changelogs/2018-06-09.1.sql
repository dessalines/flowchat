--liquibase formatted sql
--changeset tyler:20

-- Add NSFW for communities and discussions

alter table discussion add column nsfw boolean default false;

drop view discussion_full_view cascade;

CREATE OR REPLACE VIEW public.discussion_full_view AS
 SELECT d.id,
    d.community_id,
    d.title,
    d.link,
    d.text_,
    d.private,
    d.nsfw,
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

CREATE OR REPLACE VIEW public.discussion_notext_view AS
 SELECT discussion_full_view.id,
    discussion_full_view.community_id,
    discussion_full_view.title,
    discussion_full_view.link,
    discussion_full_view.private,
    discussion_full_view.nsfw,
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

--rollback alter table community drop column nsfw cascade;
--rollback drop view discussion_full_view cascade;
--rollback CREATE OR REPLACE VIEW public.discussion_full_view AS SELECT d.id, d.community_id, d.title, d.link, d.text_, d.private, d.deleted, avg(dr.rank) AS avg_rank, count(DISTINCT dr.id) AS number_of_votes, count(DISTINCT c.id) AS number_of_comments, array_agg(dt.tag_id) AS tag_ids, d.modified_by_user_id, u.name AS modified_by_user_name, d.created, d.modified FROM ((((public.discussion d LEFT JOIN public.discussion_tag dt ON ((dt.discussion_id = d.id))) LEFT JOIN public.discussion_rank dr ON ((dr.discussion_id = d.id))) LEFT JOIN public.user_ u ON ((u.id = d.modified_by_user_id))) LEFT JOIN public.comment c ON ((c.discussion_id = d.id))) GROUP BY d.id, d.title, d.link, d.text_, d.private, d.deleted, d.created, d.modified, dt.discussion_id, u.name ORDER BY d.id;

--rollback CREATE OR REPLACE VIEW public.discussion_notext_view AS SELECT discussion_full_view.id, discussion_full_view.community_id, discussion_full_view.title, discussion_full_view.link, discussion_full_view.private, discussion_full_view.deleted, discussion_full_view.avg_rank, discussion_full_view.number_of_votes, discussion_full_view.number_of_comments, discussion_full_view.tag_ids, discussion_full_view.modified_by_user_id, discussion_full_view.modified_by_user_name, discussion_full_view.created, discussion_full_view.modified FROM public.discussion_full_view;


alter table community add column nsfw boolean default false;

drop view community_view cascade;

CREATE OR REPLACE VIEW public.community_view AS
 SELECT c.id,
    c.name,
    c.text_,
    c.private,
    c.nsfw,
    c.deleted,
    avg(cr.rank) AS avg_rank,
    count(DISTINCT cr.id) AS number_of_votes,
    array_agg(ct.tag_id) AS tag_ids,
    c.created,
    c.modified
   FROM ((public.community c
     LEFT JOIN public.community_tag ct ON ((ct.community_id = c.id)))
     LEFT JOIN public.community_rank cr ON ((cr.community_id = c.id)))
  GROUP BY c.id, c.name, c.text_, c.private, c.deleted, c.created, c.modified
  ORDER BY c.id;

CREATE OR REPLACE VIEW public.community_notext_view AS
 SELECT community_view.id,
    community_view.name,
    community_view.private,
    community_view.nsfw,
    community_view.deleted,
    community_view.avg_rank,
    community_view.number_of_votes,
    community_view.tag_ids,
    community_view.created,
    community_view.modified
   FROM public.community_view;

--rollback CREATE OR REPLACE VIEW public.community_view AS SELECT c.id, c.name, c.text_, c.private, c.deleted, avg(cr.rank) AS avg_rank, count(DISTINCT cr.id) AS number_of_votes, array_agg(ct.tag_id) AS tag_ids, c.created, c.modified FROM ((public.community c LEFT JOIN public.community_tag ct ON ((ct.community_id = c.id))) LEFT JOIN public.community_rank cr ON ((cr.community_id = c.id))) GROUP BY c.id, c.name, c.text_, c.private, c.deleted, c.created, c.modified ORDER BY c.id;

--rollback CREATE OR REPLACE VIEW public.community_notext_view AS SELECT community_view.id, community_view.name, community_view.private, community_view.deleted, community_view.avg_rank, community_view.number_of_votes, community_view.tag_ids, community_view.created, community_view.modified FROM public.community_view;

--rollback alter table discussion drop column nsfw;

