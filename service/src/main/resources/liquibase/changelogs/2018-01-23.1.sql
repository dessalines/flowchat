--liquibase formatted sql
--changeset tyler:16

alter table only user_setting alter column default_view_type_id set default 1;

--rollback alter table only user_setting alter column default_view_type_id set default 2;
