package com.chat.types;

import com.chat.db.Tables;
import com.chat.db.Transformations;
import org.javalite.activejdbc.LazyList;
import org.javalite.activejdbc.Model;

import java.util.*;

/**
 * Created by tyler on 6/7/16.
 */
public class Comments implements JSONWriter {
    private List<CommentObj> comments;

    private Comments(List<CommentObj> comments) {
        this.comments = comments;
    }

    public static Comments create(LazyList<? extends Model> comments, Map<Long, Integer> votes) {

        List<CommentObj> commentObjs = Transformations.convertCommentsToEmbeddedObjects(comments, votes);

        return new Comments(commentObjs);
    }

    public static Comments replies(LazyList<? extends Model> comments) {
        Set<CommentObj> commentObjs = new LinkedHashSet<>();
        for (Model c : comments) {
            commentObjs.add(CommentObj.create(c, null));
        }

        // Convert to a list
        List<CommentObj> list = new ArrayList<>(commentObjs);

        return new Comments(list);

    }

    public List<CommentObj> getComments() {
        return comments;
    }
}
