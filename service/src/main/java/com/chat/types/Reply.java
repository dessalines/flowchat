package com.chat.types;

import com.chat.tools.Tools;

import java.io.IOException;

/**
 * Created by tyler on 6/7/16.
 */
public class Reply implements JSONWriter {
    private Long parentId;
    private String reply;

    public Reply(Long parentId, String reply) {
        this.parentId = parentId;
        this.reply = reply;
    }

    public Reply() {}

    public Long getParentId() {
        return parentId;
    }

    public String getReply() {
        return reply;
    }

    public static Reply fromJson(String replyDataStr) {

        try {
            return Tools.JACKSON.readValue(replyDataStr, Reply.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
