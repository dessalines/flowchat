package com.chat.types;

import com.chat.DataSources;
import com.chat.db.Tables.*;
import com.chat.tools.Tools;
import org.javalite.activejdbc.Model;

/**
 * Created by tyler on 6/27/16.
 */
public class RankingConstantsObj {

    private Double createdWeight, numberOfVotesWeight, avgRankWeight;

    private RankingConstantsObj(Double createdWeight, Double numberOfVotesWeight, Double avgRankWeight) {
        this.createdWeight = createdWeight;
        this.numberOfVotesWeight = numberOfVotesWeight;
        this.avgRankWeight = avgRankWeight;
    }

    public static void writeRankingConstantsToDBFromPropertiesFile() {

        Tools.dbInit();
        RankingConstants rc = RankingConstants.findFirst("id = 1");
        rc.set("created_weight", Double.valueOf(DataSources.PROPERTIES.getProperty("sorting_created_weight")),
                "number_of_votes_weight", Double.valueOf(DataSources.PROPERTIES.getProperty("sorting_number_of_votes_weight")),
                "avg_rank_weight", Double.valueOf(DataSources.PROPERTIES.getProperty("sorting_avg_rank_weight")));
        rc.saveIt();
        Tools.dbClose();

    }

    private static RankingConstantsObj create(RankingConstants rc) {
        return new RankingConstantsObj(rc.getDouble("created_weight"),
                rc.getDouble("number_of_votes_weight"),
                rc.getDouble("avg_rank_weight"));

    }

    public static RankingConstantsObj fetchRankingConstants() {

        Tools.dbInit();
        RankingConstants rc = RankingConstants.findFirst("1=1");
        RankingConstantsObj rco = create(rc);
        Tools.dbClose();

        return rco;
    }

    public Double getCreatedWeight() {
        return createdWeight;
    }
    public Double getNumberOfVotesWeight() {
        return numberOfVotesWeight;
    }
    public Double getAvgRankWeight() {
        return avgRankWeight;
    }
}
