package com.chat.types.websocket.input;

import com.chat.tools.Tools;
import com.chat.types.JSONWriter;

import java.io.IOException;

/**
 * Created by tyler on 7/11/16.
 */
public class NextPageData implements JSONWriter {
    private Long topLimit, maxDepth;

    public NextPageData() {}

    public static NextPageData fromJson(String nextPageDataStr) {

        try {
            return Tools.JACKSON.readValue(nextPageDataStr, NextPageData.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Long getTopLimit() {
        return topLimit;
    }
    public Long getMaxDepth() {return maxDepth;}

}

