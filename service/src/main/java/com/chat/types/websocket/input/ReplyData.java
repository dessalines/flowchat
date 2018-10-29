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

    public ReplyData(Long parentId, String reply) {
        this.parentId = parentId;
        this.reply = reply;
    }

    public Long getParentId() {
        return parentId;
    }

    public String getReply() {
        return reply;
    }

}
