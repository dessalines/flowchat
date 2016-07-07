package com.chat.webservice;

import com.chat.types.RankingConstantsObj;

/**
 * Created by tyler on 7/5/16.
 */
public enum ConstantsService {
    INSTANCE;

    private RankingConstantsObj rco;

    ConstantsService() {
        rco = RankingConstantsObj.fetchRankingConstants();
    }

    public RankingConstantsObj getRankingConstants() {
        return rco;
    }

}
