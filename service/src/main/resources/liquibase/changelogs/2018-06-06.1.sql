--liquibase formatted sql
--changeset tyler:19

alter table user_ add column email varchar(255) unique, add column password_encrypted varchar(512);

update user_
set email = fu.email, password_encrypted = fu.password_encrypted 
from full_user as fu
where user_.id = fu.user_id;

--rollback alter table user_ drop column email, drop column password_encrypted;

create table if not exists full_user_deprecated as select * from full_user;

drop table full_user cascade;

--nada CREATE TABLE public.full_user ( id bigint NOT NULL, user_id bigint NOT NULL, email character varying(255), password_encrypted character varying(512), created timestamp without time zone DEFAULT CURRENT_TIMESTAMP);ALTER TABLE ONLY public.full_user ADD CONSTRAINT full_user_email_key UNIQUE (email); ALTER TABLE ONLY public.full_user ADD CONSTRAINT full_user_pkey PRIMARY KEY (id); ALTER TABLE ONLY public.full_user ADD CONSTRAINT fk1_full_user_user FOREIGN KEY (user_id) REFERENCES public.user_(id) ON UPDATE CASCADE ON DELETE CASCADE;

--rollback create table if not exists full_user as select * from full_user_deprecated;

--rollback CREATE VIEW public.user_login_view AS SELECT u.id, u.name, fu.email, fu.id AS full_user_id, l.id AS login_id, l.auth, l.expire_time, u.created FROM ((public.user_ u LEFT JOIN public.full_user fu ON ((fu.user_id = u.id))) JOIN public.login l ON ((l.user_id = u.id)));

alter table login rename column auth to jwt;

--rollback alter table login rename column jwt to auth;







