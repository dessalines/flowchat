--liquibase formatted sql
--changeset tyler:23

-- Adding theme

alter table user_setting add column theme smallint not null default 0;

--rollback alter table user_setting drop column theme;

