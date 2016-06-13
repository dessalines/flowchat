package com.chat.types;

import org.javalite.activejdbc.LazyList;
import org.javalite.activejdbc.Model;

import static com.chat.db.Tables.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tyler on 6/13/16.
 */
public class CommentVotes implements JSONWriter {
    private List<CommentRankData> commentVotes;

    public CommentVotes(LazyList<CommentRank> commentRanks) {
        commentVotes = new ArrayList<>();

        for (CommentRank cr : commentRanks) {
            CommentRankData crd = new CommentRankData(cr.getInteger("rank"), cr.getLong("comment_id"));
            commentVotes.add(crd);
        }
    }

    public List<CommentRankData> getCommentVotes() {
        return commentVotes;
    }

}
