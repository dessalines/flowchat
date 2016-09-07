package com.chat.types.community;

import com.chat.db.Tables;
import com.chat.db.Transformations;
import com.chat.types.JSONWriter;
import org.javalite.activejdbc.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by tyler on 8/18/16.
 */
public class Communities implements JSONWriter {

    private List<Community> communities;
    private Long count;

    private Communities(List<Community> communities, Long count) {
        this.count = count;
        this.communities = communities;
    }

    public static Communities create(List<? extends Model> communities,
                                     List<Tables.CommunityTagView> communityTags,
                                     List<Tables.CommunityUserView> communityUsers,
                                     List<Tables.CommunityRank> communityRanks,
                                     Long count) {

        // Build maps keyed by community_id of the votes, tags, and users
        Map<Long, Integer> votes = (communityRanks != null) ?
                Transformations.convertRankToMap(communityRanks, "community_id") : null;

        Map<Long, List<Tables.CommunityTagView>> tagMap = (communityTags != null) ?
                Transformations.convertRowsToMap(communityTags, "community_id") : null;

        Map<Long, List<Tables.CommunityUserView>> userMap = (communityUsers != null) ?
                Transformations.convertRowsToMap(communityUsers, "community_id") : null;

        // Convert to a list of community objects
        List<Community> cos = new ArrayList<>();

        for (Model view : communities) {
            Long id = view.getLongId();
            Integer vote = (votes != null && votes.get(id) != null) ? votes.get(id) : null;
            List<Tables.CommunityTagView> tags = (tagMap != null && tagMap.get(id) != null) ? tagMap.get(id) : null;
            List<Tables.CommunityUserView> users = (userMap != null && userMap.get(id) != null) ? userMap.get(id) : null;
            Community c = Community.create(view, tags, users, vote);
            cos.add(c);
        }

        return new Communities(cos, count);
    }

    public Long getCount() {
        return count;
    }

    public List<Community> getCommunities() {
        return communities;
    }
}
