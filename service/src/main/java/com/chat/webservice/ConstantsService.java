package com.chat.webservice;

import com.chat.tools.Tools;
import com.chat.types.RankingConstantsObj;

/**
 * Created by tyler on 7/5/16.
 */
public enum ConstantsService {
    INSTANCE;

    private RankingConstantsObj rco;

    ConstantsService() {

        // Need to write the sorting comments from the properties file to the DB
        // They are used both in code, and in the DB
        RankingConstantsObj.writeRankingConstantsToDBFromPropertiesFile();

        // Fetch them from the DB
        rco = RankingConstantsObj.fetchRankingConstants();

    }

    public RankingConstantsObj getRankingConstants() {
        return rco;
    }

}
