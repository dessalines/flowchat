package com.chat.webservice;

import com.chat.types.RankingConstants;

/**
 * Created by tyler on 7/5/16.
 */
public enum ConstantsService {
    INSTANCE;

    private RankingConstants rco;

    ConstantsService() {

        // Need to write the sorting comments from the properties file to the DB
        // They are used both in code, and in the DB
        RankingConstants.writeRankingConstantsToDBFromPropertiesFile();

        // Fetch them from the DB
        rco = RankingConstants.fetchRankingConstants();

    }

    public RankingConstants getRankingConstants() {
        return rco;
    }

}
