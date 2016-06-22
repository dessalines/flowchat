package com.chat.types;

import com.chat.db.Transformations;
import org.javalite.activejdbc.LazyList;
import org.javalite.activejdbc.Model;

import java.util.List;
import java.util.Map;

/**
 * Created by tyler on 6/22/16.
 */
public class Discussions implements JSONWriter {
    private List<DiscussionObj> discussions;

    public Discussions(LazyList<? extends Model> discussions, Map<Long, Integer> votes) {
        this.discussions = Transformations.convertDiscussionsToObjects(discussions, votes);
    }

    public List<DiscussionObj> getDiscussions() {
        return discussions;
    }
}