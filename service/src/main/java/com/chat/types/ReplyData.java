package com.chat.types;

import com.chat.tools.Tools;

import java.io.IOException;

/**
 * Created by tyler on 6/7/16.
 */
public class ReplyData implements JSONWriter {
    private Long parentId;
    private String reply;

    public ReplyData(Long parentId, String reply) {
        this.parentId = parentId;
        this.reply = reply;
    }

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
