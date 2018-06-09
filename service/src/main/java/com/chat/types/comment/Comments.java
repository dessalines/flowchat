package com.chat.types.comment;

import com.chat.db.Transformations;
import com.chat.types.JSONWriter;
import org.javalite.activejdbc.LazyList;
import org.javalite.activejdbc.Model;

import java.util.*;

/**
 * Created by tyler on 6/7/16.
 */
public class Comments implements JSONWriter {
    private List<Comment> comments;

    private Comments(List<Comment> comments) {
        this.comments = comments;
    }

    public static Comments create(
            LazyList<? extends Model> comments,
            Map<Long, Integer> votes,
            Long topLimit, Long maxDepth, Comparator<Comment> comparator) {

        List<Comment> commentObjs = Transformations.convertCommentsToEmbeddedObjects(
                comments, votes, topLimit, maxDepth, comparator);

        return new Comments(commentObjs);
    }

    public static Comments replies(LazyList<? extends Model> comments) {
        Set<Comment> commentObjs = new LinkedHashSet<>();
        for (Model c : comments) {
            commentObjs.add(Comment.create(c, null));
        }

        // Convert to a list
        List<Comment> list = new ArrayList<>(commentObjs);

        return new Comments(list);

    }

    public List<Comment> getComments() {
        return comments;
    }

}
