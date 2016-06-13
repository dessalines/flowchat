package com.chat.types;

import com.chat.db.Tables;
import com.chat.db.Transformations;
import org.javalite.activejdbc.LazyList;
import org.javalite.activejdbc.Model;

import java.util.List;
import java.util.Map;

/**
 * Created by tyler on 6/7/16.
 */
public class Comments implements JSONWriter {
    private List<CommentObj> comments;

    public Comments(LazyList<? extends Model> comments, Map<Long, Integer> votes) {
        this.comments = Transformations.convertCommentsToEmbeddedObjects(comments, votes);
    }

    public List<CommentObj> getComments() {
        return comments;
    }
}
