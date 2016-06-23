package com.chat.types;

import org.javalite.activejdbc.LazyList;
import org.javalite.activejdbc.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by tyler on 6/22/16.
 */
public class Discussions implements JSONWriter {
    private List<DiscussionObj> discussions;

    public Discussions(LazyList<? extends Model> discussions, Map<Long, Integer> votes) {
        this.discussions = create(discussions, votes);
    }

    public static List<DiscussionObj> create(LazyList<? extends Model> discussions,
                                             Map<Long, Integer> votes) {
        // Convert to a list of discussion objects
        List<DiscussionObj> dos = new ArrayList<>();

        for (Model view : discussions) {
            Long id = view.getLongId();
            Integer vote = (votes.get(id) != null) ? votes.get(id) : null;
            DiscussionObj df = DiscussionObj.create(view, vote);
            dos.add(df);
        }

        return dos;
    }


    public List<DiscussionObj> getDiscussions() {
        return discussions;
    }
}