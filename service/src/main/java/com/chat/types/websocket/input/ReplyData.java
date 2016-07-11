package com.chat.types.websocket.input;

import com.chat.tools.Tools;
import com.chat.types.JSONWriter;

import java.io.IOException;

/**
 * Created by tyler on 6/7/16.
 */
public class ReplyData implements JSONWriter {
    private Long parentId;
    private String reply;

    public ReplyData() {}

    public Long getParentId() {
        return parentId;
    }

    public String getReply() {
        return reply;
    }

    public static ReplyData fromJson(String replyDataStr) {

        try {
            return Tools.JACKSON.readValue(replyDataStr, ReplyData.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
