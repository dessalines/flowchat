--liquibase formatted sql
--changeset tyler:7

alter view popular_tags_view rename to tags_view;

-- rollback alter view tags_view rename to popular_tags_view;
