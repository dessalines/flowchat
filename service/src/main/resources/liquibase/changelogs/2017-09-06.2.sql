--liquibase formatted sql
--changeset tyler:14

create or replace function ranking(created timestamp default now(), created_weight numeric(20,8) default 0,
number_of_votes bigint default 0, number_of_votes_weight numeric(20,8) default 0,
avg_rank numeric default 0, avg_rank_weight numeric(20,8) default 0
)
returns numeric(20,8) as $$
select
    cast(exp(extract(epoch from (created - now()))/created_weight) as numeric(20,8)) +
    cast(coalesce(number_of_votes,0)*number_of_votes_weight as numeric(20,8)) +
    cast(coalesce(avg_rank,0)*avg_rank_weight as numeric(20,8))
$$ LANGUAGE SQL IMMUTABLE;

--rollback drop function ranking(timestamp, numeric, bigint, numeric, numeric, numeric);