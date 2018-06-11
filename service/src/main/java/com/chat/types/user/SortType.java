package com.chat.types.user;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by tyler on 9/23/16.
 */

// TODO alter the front end code to use numbers as the radio values, and then do the string inflation from this code
public enum SortType {
    NEW(1, "created__desc"),
    HOUR(2, "time-3600"),
    DAY(3, "time-86400"),
    WEEK(4, "time-604800"),
    MONTH(5, "time-2628000"),
    YEAR(6, "time-31540000"),
    ALLTIME(7, "number_of_votes__desc");

    Integer val;
    String radioValue;
    SortType(Integer val, String radioValue) {
        this.val = val;
        this.radioValue = radioValue;
    }

    public int getVal() {
        return val;
    }
    public String getRadioValue() { return radioValue;}


    public static SortType getFromRadioValue(String radioValue) {
        Map<String, SortType> sortTypeMap = new LinkedHashMap<>();

        for (SortType st : SortType.values()) {
            sortTypeMap.put(st.getRadioValue(), st);
        }

        return sortTypeMap.get(radioValue);
    }
}
