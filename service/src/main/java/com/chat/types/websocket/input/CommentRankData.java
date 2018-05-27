package com.chat.types.websocket.input;

import com.chat.tools.Tools;
import com.chat.types.JSONWriter;

import java.io.IOException;

/**
 * Created by tyler on 6/13/16.
 */
public class CommentRankData implements JSONWriter {
    private Integer rank;
    private Long commentId;

    public CommentRankData() {}

    public CommentRankData(Integer rank, Long commentId) {
        this.rank = rank;
        this.commentId = commentId;
    }

    public Integer getRank() {
        return rank;
    }

    public Long getCommentId() {
        return commentId;
    }

    public static CommentRankData fromJson(String dataStr) {
        try {
            return Tools.JACKSON.readValue(dataStr, CommentRankData.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
