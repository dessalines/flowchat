package com.chat.types.discussion;

import com.chat.db.Tables;
import com.chat.db.Transformations;
import com.chat.types.JSONWriter;
import org.javalite.activejdbc.Model;

import java.util.*;

/**
 * Created by tyler on 6/22/16.
 */
public class Discussions implements JSONWriter {
    private Set<Discussion> discussions;
    private Long count;

    private Discussions(Set<Discussion> discussions, Long count) {
        this.count = count;
        this.discussions = discussions;
    }

    public static Discussions create(List<? extends Model> discussions,
                                     List<Tables.CommunityNoTextView> communities,
                                     List<Tables.DiscussionTagView> discussionTags,
                                     List<Tables.DiscussionUserView> discussionUsers,
                                     List<Tables.DiscussionRank> discussionRanks,
                                     Long count) {

        // Build maps keyed by discussion_id of the votes, tags, and users
        Map<Long, Integer> votes = (discussionRanks != null) ?
                Transformations.convertRankToMap(discussionRanks, "discussion_id") : null;

        Map<Long, List<Tables.DiscussionTagView>> tagMap = (discussionTags != null) ?
                Transformations.convertRowsToMap(discussionTags, "discussion_id") : null;

        Map<Long, List<Tables.DiscussionUserView>> userMap = (discussionUsers != null) ?
                Transformations.convertRowsToMap(discussionUsers, "discussion_id") : null;

        // Convert to a list of discussion objects
        Set<Discussion> dos = new LinkedHashSet<>();

        for (Model view : discussions) {
            Long id = view.getLongId();
            Integer vote = (votes != null && votes.get(id) != null) ? votes.get(id) : null;
            List<Tables.DiscussionTagView> tags = (tagMap != null && tagMap.get(id) != null) ? tagMap.get(id) : null;
            List<Tables.DiscussionUserView> users = (userMap != null && userMap.get(id) != null) ? userMap.get(id) : null;
            Tables.CommunityNoTextView community = null;
            if (communities != null) {
                for (Tables.CommunityNoTextView cntv : communities) {
                    if (view.getLong("community_id").equals(cntv.getLongId())) {
                        community = cntv;
                        break;
                    }
                }
            }

            // TODO should the list of discussions also filter for blocked communities?
            Discussion df = Discussion.create(view, community, tags, users, null, vote);
            dos.add(df);
        }

        return new Discussions(dos, count);
    }

    public Long getCount() {
        return count;
    }

    public Set<Discussion> getDiscussions() {
        return discussions;
    }
}