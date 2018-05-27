package com.chat.types.user;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by tyler on 9/23/16.
 */
public enum ViewType {

    CARD(1, "card"), LIST(2, "list");

    Integer val;
    String radioValue;

    ViewType(int val, String radioValue) {
        this.val = val;
        this.radioValue = radioValue;
    }

    public int getVal() {
        return val;
    }
    public String getRadioValue() { return radioValue;}


    public static ViewType getFromRadioValue(String radioValue) {
        Map<String, ViewType> viewTypeMap = new LinkedHashMap<>();

        for (ViewType vt : ViewType.values()) {
            viewTypeMap.put(vt.getRadioValue(), vt);
        }

        return viewTypeMap.get(radioValue);
    }
}
