package com.chat.types;

import com.chat.db.Tables;
import com.chat.tools.Tools;
import org.javalite.activejdbc.Model;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Created by tyler on 8/18/16.
 */
public class CommunityObj implements JSONWriter {

    private Long id;
    private UserObj creator;
    private String name, text;
    private Boolean private_, deleted;
    private Integer avgRank, userRank, numberOfVotes;
    private List<TagObj> tags;
    private List<UserObj> privateUsers, blockedUsers;
    private Timestamp created, modified;

    public CommunityObj() {}

    public CommunityObj(Long id,
                         String name,
                         String text,
                         Boolean private_,
                         Integer avgRank,
                         Integer userRank,
                         Integer numberOfVotes,
                         List<TagObj> tags,
                         UserObj creator,
                         List<UserObj> privateUsers,
                         List<UserObj> blockedUsers,
                         Boolean deleted,
                         Timestamp created,
                         Timestamp modified) {
        this.id = id;
        this.creator = creator;
        this.name = name;
        this.text = text;
        this.private_ = private_;
        this.avgRank = avgRank;
        this.userRank = userRank;
        this.numberOfVotes = numberOfVotes;
        this.tags = tags;
        this.creator = creator;
        this.privateUsers = privateUsers;
        this.blockedUsers = blockedUsers;
        this.deleted = deleted;
        this.created = created;
        this.modified = modified;


    }

    public void checkPrivate(UserObj userObj) {
        if (getPrivate_().equals(true)) {
            if (!getPrivateUsers().contains(userObj)) {
                throw new NoSuchElementException("Private community, not allowed to view");
            }
        }
    }

    public void checkBlocked(UserObj userObj) {
        System.out.println(Arrays.toString(getBlockedUsers().toArray()));
        if (getBlockedUsers().contains(userObj)) {
            throw new NoSuchElementException("You have been blocked from this community");
        }
    }

    public static CommunityObj create(Model c,
                                       List<Tables.CommunityTagView> communityTags,
                                       List<Tables.CommunityUserView> userCommunity,
                                       Integer vote) {
        // convert the tags
        List<TagObj> tags = null;
        if (discussionTags != null) {
            tags = new ArrayList<>();
            for (Tables.DiscussionTagView dtv : discussionTags) {
                tags.add(TagObj.create(dtv.getLong("tag_id"), dtv.getString("name")));
            }
        }

        // convert the user discussion roles
        UserObj creator = null;
        List<UserObj> privateUsers = new ArrayList<>();
        List<UserObj> blockedUsers = new ArrayList<>();

        if (userDiscussions != null) {
            for (Tables.DiscussionUserView udv : userDiscussions) {

                DiscussionRole role = DiscussionRole.values()[udv.getLong("discussion_role_id").intValue() - 1];

                UserObj userObj = UserObj.create(udv.getLong("user_id"), udv.getString("name"));

                switch (role) {
                    case BLOCKED:
                        blockedUsers.add(userObj);
                        break;
                    case USER:
                        privateUsers.add(userObj);
                        break;
                    case CREATOR:
                        creator = userObj;
                        break;
                }
            }
        }

        return new DiscussionObj(d.getLongId(),
                d.getString("title"),
                d.getString("link"),
                d.getString("text_"),
                d.getBoolean("private"),
                d.getInteger("avg_rank"),
                vote,
                d.getInteger("number_of_votes"),
                tags,
                creator,
                privateUsers,
                blockedUsers,
                d.getBoolean("deleted"),
                d.getTimestamp("created"),
                d.getTimestamp("modified"));
    }

    public static DiscussionObj fromJson(String dataStr) {

        try {
            return Tools.JACKSON.readValue(dataStr, DiscussionObj.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getText() {
        return text;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public Boolean getPrivate_() {
        return private_;
    }

    public Integer getAvgRank() {
        return avgRank;
    }

    public Integer getUserRank() {
        return userRank;
    }

    public Integer getNumberOfVotes() {
        return numberOfVotes;
    }

    public List<TagObj> getTags() {
        return tags;
    }

    public List<UserObj> getPrivateUsers() {
        return privateUsers;
    }

    public List<UserObj> getBlockedUsers() {
        return blockedUsers;
    }

    public Timestamp getCreated() {
        return created;
    }

    public Timestamp getModified() {
        return modified;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public UserObj getCreator() {return creator;}
}
