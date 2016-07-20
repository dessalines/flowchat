
select *,extract(epoch from (created - now())),
ranking(created, 3600,
                    number_of_votes,  .001
                    ,avg_rank,  0.01
                    )
from discussion_notext_view
order by ranking(created, 3600,
                    number_of_votes,  .001
                    ,avg_rank,  0.01
                    ) desc nulls last;

                    -- 31540000, 2628000, 604800, 86400, 3600
                    -- .001, 0.01