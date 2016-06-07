package com.chat.types;

import com.chat.db.Tables;
import com.chat.db.Transformations;
import org.javalite.activejdbc.LazyList;

import java.util.List;

/**
 * Created by tyler on 6/7/16.
 */
public class Comments implements JSONWriter {
    private List<CommentObj> comments;

    public Comments(LazyList<Tables.CommentThreadedView> comments) {
        this.comments = Transformations.convertCommentsToEmbeddedObjects(comments);
    }

    public List<CommentObj> getComments() {
        return comments;
    }
}
