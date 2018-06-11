package com.chat.types.user;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by tyler on 9/23/16.
 */

// TODO alter the front end code to use numbers as the radio values, and then do the string inflation from this code
public enum CommentSortType {
    NEW(1, "new"),
    HOT(2, "hot"),
    TOP(3, "top");

    Integer val;
    String radioValue;
    CommentSortType(Integer val, String radioValue) {
        this.val = val;
        this.radioValue = radioValue;
    }

    public int getVal() {
        return val;
    }
    public String getRadioValue() { return radioValue;}

    public static CommentSortType getFromRadioValue(String radioValue) {
        Map<String, CommentSortType> sortTypeMap = new LinkedHashMap<>();

        for (CommentSortType st : CommentSortType.values()) {
            sortTypeMap.put(st.getRadioValue(), st);
        }

        return sortTypeMap.get(radioValue);
    }
}
