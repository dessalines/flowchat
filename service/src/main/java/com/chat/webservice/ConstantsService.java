package com.chat.webservice;

import com.chat.db.Tables;
import com.chat.tools.Tools;
import com.chat.types.RankingConstants;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tyler on 7/5/16.
 */
public enum ConstantsService {
    INSTANCE;

    private RankingConstants rco;
    private String censoredRegex;

    ConstantsService() {

        // Need to write the sorting comments from the properties file to the DB
        // They are used both in code, and in the DB
        RankingConstants.writeRankingConstantsToDBFromPropertiesFile();

        // Fetch them from the DB
        rco = RankingConstants.fetchRankingConstants();

        censoredRegex = fetchCensoredWords();

    }

    public RankingConstants getRankingConstants() {
        return rco;
    }

    public String getCensoredRegex() { return censoredRegex; }

    public String replaceCensoredText(String s) {
        if (s != null) {
            return s.replaceAll(censoredRegex, "*removed*");
        } else {
            return null;
        }
    }

    private String fetchCensoredWords() {
        Tools.dbInit();
        Set<String> words = Tables.CensoredWord.findAll().collectDistinct("regex");
        Tools.dbClose();

        return String.join("|", words);
    }

}
