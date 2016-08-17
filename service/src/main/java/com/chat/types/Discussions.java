package com.chat.types;

import com.chat.db.Tables;
import com.chat.db.Transformations;
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
    private Long count;

    private Discussions(List<DiscussionObj> discussions, Long count) {
        this.count = count;
        this.discussions = discussions;
    }

    public static Discussions create(List<? extends Model> discussions,
                                     List<Tables.DiscussionTagView> discussionTags,
                                     List<Tables.UserDiscussionView> userDiscussions,
                                     List<Tables.DiscussionRank> discussionRanks, Long count) {

        // Build maps keyed by discussion_id of the votes, tags, and users
        Map<Long, Integer> votes = (discussionRanks != null) ?
                Transformations.convertDiscussionRankToMap(discussionRanks) : null;

        Map<Long, List<Tables.DiscussionTagView>> tagMap = (discussionTags != null) ?
                Transformations.convertDiscussionRowsToMap(discussionTags) : null;

        Map<Long, List<Tables.UserDiscussionView>> userMap = (userDiscussions != null) ?
                Transformations.convertDiscussionRowsToMap(userDiscussions) : null;

        // Convert to a list of discussion objects
        List<DiscussionObj> dos = new ArrayList<>();

        for (Model view : discussions) {
            Long id = view.getLongId();
            Integer vote = (votes != null && votes.get(id) != null) ? votes.get(id) : null;
            List<Tables.DiscussionTagView> tags = (tagMap != null && tagMap.get(id) != null) ? tagMap.get(id) : null;
            List<Tables.UserDiscussionView> users = (userMap != null && userMap.get(id) != null) ? userMap.get(id) : null;
            DiscussionObj df = DiscussionObj.create(view, tags, users, vote);
            dos.add(df);
        }

        return new Discussions(dos, count);
    }

    public Long getCount() {
        return count;
    }

    public List<DiscussionObj> getDiscussions() {
        return discussions;
    }
}