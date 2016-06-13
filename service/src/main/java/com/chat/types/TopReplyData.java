package com.chat.types;

import com.chat.tools.Tools;

import java.io.IOException;

/**
 * Created by tyler on 6/13/16.
 */
public class TopReplyData implements JSONWriter {
    private String topReply;

    public TopReplyData(String topReply) {
        this.topReply = topReply;
    }

    public TopReplyData() {}



    public String getTopReply() {
        return topReply;
    }

    public static TopReplyData fromJson(String replyDataStr) {

        try {
            return Tools.JACKSON.readValue(replyDataStr, TopReplyData.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}