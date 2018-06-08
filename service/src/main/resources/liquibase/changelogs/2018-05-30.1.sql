--liquibase formatted sql
--changeset tyler:17

--  Getting rid of all the views

drop view comment_threaded_view;
drop view comment_breadcrumbs_view;
drop view comment_intermediate_view;

--rollback create view children_view as sELECT p.parent_id, count(*) - 1 AS num_of_children FROM comment_tree p GROUP BY p.parent_id ORDER BY p.parent_id;

--rollback create view comment_intermediate_view as SELECT d.id,d.user_id,u.name AS user_name,d.modified_by_user_id,u2.name AS modified_by_user_name,d.discussion_id,d.text_,p.path_length,p.parent_id,p.child_id,array_agg(crumbs.parent_id ORDER BY crumbs.parent_id) AS breadcrumbs,count(crumbs.parent_id) - 1 AS num_of_parents,cv.num_of_children,d.deleted,d.read,d.created,d.modified FROM comment d JOIN comment_tree p ON d.id = p.child_id JOIN comment_tree crumbs ON crumbs.child_id = p.child_id JOIN children_view cv ON d.id = cv.parent_id JOIN user_ u ON d.user_id = u.id JOIN user_ u2 ON d.modified_by_user_id = u2.id GROUP BY d.id, p.path_length, p.parent_id, p.child_id, cv.num_of_children, u.name, u2.name ORDER BY (array_agg(crumbs.parent_id ORDER BY crumbs.parent_id));

--rollback create view comment_breadcrumbs_view as SELECT d.id,d.user_id,d.user_name,d.modified_by_user_id,d.modified_by_user_name,d.discussion_id,d.text_,d.path_length,d.parent_id,d.child_id,d.breadcrumbs,d.num_of_parents,d.num_of_children,d.deleted,d.read,d.created,d.modified,d2.user_id AS parent_user_id,avg(cr.rank) AS avg_rank,count(cr.rank) AS number_of_votes FROM comment_intermediate_view d JOIN comment d2 ON d.parent_id = d2.id LEFT JOIN comment_rank cr ON d.id = cr.comment_id GROUP BY d.id, d2.user_id, d.user_id, d.user_name, d.modified_by_user_id, d.modified_by_user_name, d.discussion_id, d.text_, d.path_length, d.parent_id,d.child_id, d.breadcrumbs, d.num_of_parents, d.num_of_children, d.deleted, d.read, d.created, d.modified ORDER BY d.breadcrumbs;

--rollback create view comment_threaded_view as  SELECT b.id,b.user_id,b.user_name,b.modified_by_user_id,b.modified_by_user_name,b.discussion_id,b.text_,b.path_length,b.parent_id,b.child_id,b.breadcrumbs,b.num_of_parents,b.num_of_children,b.deleted,b.read,b.created,b.modified,b.parent_user_id,b.avg_rank,b.number_of_votes FROM comment_breadcrumbs_view a JOIN comment_breadcrumbs_view b ON a.id = b.parent_id AND a.num_of_parents = 0 ORDER BY a.id, b.breadcrumbs;

drop view discussion_notext_view;

--rollback create view discussion_full_view as SELECT d.id, d.community_id, d.title, d.link, d.text_, d.private, d.deleted, avg(dr.rank) AS avg_rank, count(DISTINCT dr.id) AS number_of_votes, count(DISTINCT c.id) AS number_of_comments, array_agg(dt.tag_id) AS tag_ids, d.modified_by_user_id, u.name AS modified_by_user_name, d.created, d.modified FROM discussion d LEFT JOIN discussion_tag dt ON dt.discussion_id = d.id LEFT JOIN discussion_rank dr ON dr.discussion_id = d.id LEFT JOIN user_ u ON u.id = d.modified_by_user_id LEFT JOIN comment c ON c.discussion_id = d.id GROUP BY d.id, d.title, d.link, d.text_, d.private, d.deleted, d.created, d.modified, dt.discussion_id, u.name ORDER BY d.id;

--rollback create view discussion_notext_view as  SELECT discussion_full_view.id, discussion_full_view.community_id, discussion_full_view.title, discussion_full_view.link, discussion_full_view.private, discussion_full_view.deleted, discussion_full_view.avg_rank, discussion_full_view.number_of_votes, discussion_full_view.number_of_comments, discussion_full_view.tag_ids, discussion_full_view.modified_by_user_id, discussion_full_view.modified_by_user_name, discussion_full_view.created, discussion_full_view.modified FROM discussion_full_view;

drop view discussion_full_view;




drop view discussion_tag_view;

--rollback create view discussion_tag_view as SELECT dt.discussion_id, dt.tag_id, t.name FROM discussion_tag dt JOIN discussion d ON d.id = dt.discussion_id JOIN tag t ON t.id = dt.tag_id;

drop view discussion_user_view;

--rollback create view discussion_user_view as SELECT ud.user_id, ud.discussion_id, u.name, ud.discussion_role_id, d.deleted FROM discussion_user ud JOIN discussion d ON d.id = ud.discussion_id JOIN user_ u ON u.id = ud.user_id;

drop view favorite_discussion_user_view;

--rollback create view favorite_discussion_user_view as SELECT ud.user_id, ud.discussion_id, u.name, d.deleted FROM favorite_discussion_user ud JOIN discussion d ON d.id = ud.discussion_id JOIN user_ u ON u.id = ud.user_id;

drop view community_notext_view;

--rollback create view community_view as SELECT c.id, c.name, c.text_, c.private, c.deleted, avg(cr.rank) AS avg_rank, count(DISTINCT cr.id) AS number_of_votes, array_agg(ct.tag_id) AS tag_ids, c.created, c.modified FROM community c LEFT JOIN community_tag ct ON ct.community_id = c.id LEFT JOIN community_rank cr ON cr.community_id = c.id GROUP BY c.id, c.name, c.text_, c.private, c.deleted, c.created, c.modified ORDER BY c.id;

--rollback create view community_notext_view as SELECT community_view.id, community_view.name, community_view.private, community_view.deleted, community_view.avg_rank, community_view.number_of_votes, community_view.tag_ids, community_view.created, community_view.modified FROM community_view;

drop view community_view;



drop view community_tag_view;

--rollback create view community_tag_view as SELECT dt.community_id, dt.tag_id, t.name FROM community_tag dt JOIN community d ON d.id = dt.community_id JOIN tag t ON t.id = dt.tag_id;

drop view community_user_view;

--rollback create view community_user_view as SELECT uc.user_id, uc.community_id, u.name, uc.community_role_id, c.deleted FROM community_user uc JOIN community c ON c.id = uc.community_id JOIN user_ u ON u.id = uc.user_id;

drop view tags_view;

--rollback create view tags_view as SELECT t.id, t.name, t.created, count(dt.tag_id) AS count FROM tag t LEFT JOIN discussion_tag dt ON dt.tag_id = t.id GROUP BY t.id;

drop view user_view;

--rollback create view user_view as SELECT u.id, u.name, fu.id AS full_user_id, fu.email, us.default_view_type_id, vt.radio_value AS default_view_type_radio_value, st.radio_value AS default_sort_type_radio_value, us.read_onboard_alert, l.id AS login_id, l.auth, l.expire_time, u.created FROM user_ u LEFT JOIN full_user fu ON u.id = fu.user_id LEFT JOIN user_setting us ON u.id = us.user_id LEFT JOIN view_type vt ON us.default_view_type_id = vt.id LEFT JOIN sort_type st ON us.default_sort_type_id = st.id LEFT JOIN login l ON l.user_id = u.id;

drop view user_login_view;

--rollback create view user_login_view as SELECT u.id, u.name, fu.email, fu.id AS full_user_id, l.id AS login_id, l.auth, l.expire_time, u.created FROM user_ u LEFT JOIN full_user fu ON fu.user_id = u.id JOIN login l ON l.user_id = u.id;

drop view children_view;








