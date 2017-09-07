create or replace function ranking(created timestamp default now(), created_weight numeric(20,8) default 0,
number_of_votes bigint default 0, number_of_votes_weight numeric(20,8) default 0,
avg_rank numeric default 0, avg_rank_weight numeric(20,8) default 0
)
returns numeric(20,8) as $$
declare res numeric(20,8);
begin
    res :=
    cast(coalesce(exp(extract(epoch from (created - now()))/created_weight), 0) as numeric(20,8)) +
    cast(coalesce(number_of_votes,0)*number_of_votes_weight as numeric(20,8)) +
    cast(coalesce(avg_rank,0)*avg_rank_weight as numeric(20,8));
    return res;
exception when others then
    return 0;
end
$$ LANGUAGE plpgsql;