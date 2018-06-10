package com.chat.types.websocket.input;

import com.chat.tools.Tools;
import com.chat.types.JSONWriter;

import java.io.IOException;

/**
 * Created by tyler on 6/11/16.
 */
public class StickyData implements JSONWriter {
    private Long id;
    private Boolean sticky;

    public StickyData(Long id, Boolean sticky) {
        this.id = id;
        this.sticky = sticky;
    }

    public StickyData() {}

    public Long getId() {
        return id;
    }

    public Boolean getSticky() {
        return sticky;
    }

    public static StickyData fromJson(String stickyDataStr) {

        try {
            return Tools.JACKSON.readValue(stickyDataStr, StickyData.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}