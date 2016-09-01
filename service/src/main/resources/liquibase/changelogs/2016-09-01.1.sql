--liquibase formatted sql
--changeset tyler:10


-- Add a modified_by_user_id column to comment, discussion, community
alter table discussion add column modified_by_user_id bigint not null default 1;
update discussion set modified_by_user_id = discussion_user.user_id from discussion_user where discussion_user.discussion_id = discussion.id;

alter table discussion add constraint fk1_discussion_modified_by_user foreign key (modified_by_user_id)
        references user_ (id)
        on update cascade on delete cascade
--rollback alter table discussion drop constraint fk1_discussion_modified_by_user;
--rollback alter table discussion drop column modified_by_user_id;


-- alter the views, and the audit view to get the modifier/deleter
