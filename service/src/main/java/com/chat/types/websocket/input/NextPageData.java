package com.chat.types.websocket.input;

import com.chat.types.JSONWriter;

import java.io.IOException;

/**
 * Created by tyler on 7/11/16.
 */
public class NextPageData implements JSONWriter {
    private Long topLimit, maxDepth;

    public NextPageData(Long topLimit, Long maxDepth) {
        this.topLimit = topLimit;
        this.maxDepth = maxDepth;
    }

    public Long getTopLimit() {
        return topLimit;
    }
    public Long getMaxDepth() {return maxDepth;}

}

