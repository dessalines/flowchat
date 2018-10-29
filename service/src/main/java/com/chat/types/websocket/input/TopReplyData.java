package com.chat.types.websocket.input;

import com.chat.tools.Tools;
import com.chat.types.JSONWriter;

import java.io.IOException;

/**
 * Created by tyler on 6/13/16.
 */
public class TopReplyData implements JSONWriter {
    private String topReply;

    public TopReplyData(String topReply) {
        this.topReply = topReply;
    }

    public String getTopReply() {
        return topReply;
    }

}