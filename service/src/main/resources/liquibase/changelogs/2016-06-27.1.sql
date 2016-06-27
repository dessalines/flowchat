
create function ranking(created timestamp default now(), created_weight numeric(10,2) default 0,
number_of_votes bigint default 0, number_of_votes_weight numeric(10,2) default 0,
avg_rank numeric default 0, avg_rank_weight numeric(10,2) default 0
)
returns numeric(10,2) as $$
select
    cast(created_weight/extract(epoch from (now() - created)) as numeric(10,2)) +
    cast(coalesce(number_of_votes,0)*number_of_votes_weight as numeric(10,2)) +
    cast(coalesce(avg_rank,0)*avg_rank_weight as numeric(10,2))
$$ LANGUAGE SQL IMMUTABLE;

--select *, ranking(created, 1000000,
--              number_of_votes, .001,
--              avg_rank, .01)
--from discussion_notext_view
--order by
--    ranking(created, 1000000,
--    number_of_votes, .001,
--    avg_rank, .01) desc;

--select extract(epoch from (now() - created)) from discussion;


-- some good defaults for weightings: 1000000, .001, .01 (trying to make them 0-1 based

--rollback drop function ranking(timestamp, numeric, bigint, numeric, numeric, numeric);
