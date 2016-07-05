package com.chat.webservice;

import com.chat.db.Tables;
import com.chat.types.RankingConstantsObj;

/**
 * Created by tyler on 7/5/16.
 */
public enum Constants {
    INSTANCE;

    private RankingConstantsObj rco;

    Constants() {
        rco = RankingConstantsObj.fetchRankingConstants();
    }

    public RankingConstantsObj getRankingConstants() {
        return rco;
    }

}
