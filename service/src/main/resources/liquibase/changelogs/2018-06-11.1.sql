--liquibase formatted sql
--changeset tyler:22

-- Adding default comment sorting user setting

-- alter default sort type

update sort_type set radio_value='created__desc' where id = 1;

--rollback update sort_type set radio_value='modified__desc' where id = 1;

alter table user_setting add column default_comment_sort_type_id bigint not null default 1;

--rollback alter table user_setting drop column default_comment_sort_type_id;

