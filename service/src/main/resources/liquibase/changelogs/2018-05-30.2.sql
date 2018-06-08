--liquibase formatted sql
--changeset tyler:18

-- Removing day sort type

delete from sort_type where radio_value ='time-3600';

--rollback insert into sort type (radio_value) values ('time-3600');

-- Adding NSFW filter

alter table discussion add column nsfw boolean not null default false after private_;

--rollback alter table discussion drop column cascade;

-- Adding slur filter per community

alter table censored_word add column community_id bigint not null default 1 after regex;

--rollback alter table censored_word drop column community_id cascade;

alter table censored_word add constraint fk1_censored_word_community foreign key (community_id)
        references community (id)
        on update cascade on delete cascade;

--rollback alter table censored_word drop constraint fk1_censored_word_community;
