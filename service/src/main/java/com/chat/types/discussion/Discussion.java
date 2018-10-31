package com.chat.types.discussion;

import com.chat.db.Tables;
import com.chat.tools.Tools;
import com.chat.types.JSONWriter;
import com.chat.types.community.Community;
import com.chat.types.tag.Tag;
import com.chat.types.user.User;
import com.chat.webservice.ConstantsService;
import org.javalite.activejdbc.Model;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by tyler on 6/19/16.
 */
public class Discussion implements JSONWriter {
    private Long id;
    private User creator, modifiedByUser;
    private String title, link, text;
    private Boolean private_, deleted, nsfw, stickied;
    private Integer avgRank, userRank, numberOfVotes, numberOfComments;
    private Community community;
    private List<Tag> tags;
    private List<User> privateUsers, blockedUsers;
    private Timestamp created, modified;

    public Discussion() {
    }

    public Discussion(Long id,
                      String title,
                      String link,
                      String text,
                      Boolean private_,
                      Boolean nsfw,
                      Boolean stickied,
                      Integer avgRank,
                      Integer userRank,
                      Integer numberOfVotes,
                      Integer numberOfComments,
                      List<Tag> tags,
                      User creator,
                      User modifiedByUser,
                      List<User> privateUsers,
                      List<User> blockedUsers,
                      Boolean deleted,
                      Community community,
                      Timestamp created,
                      Timestamp modified) {
        this.id = id;
        this.creator = creator;
        this.modifiedByUser = modifiedByUser;
        this.title = title;
        this.link = link;
        this.text = text;
        this.private_ = private_;
        this.nsfw = nsfw;
        this.stickied = stickied;
        this.avgRank = avgRank;
        this.userRank = userRank;
        this.numberOfVotes = numberOfVotes;
        this.numberOfComments = numberOfComments;
        this.tags = tags;
        this.creator = creator;
        this.privateUsers = privateUsers;
        this.blockedUsers = blockedUsers;
        this.deleted = deleted;
        this.community = community;
        this.created = created;
        this.modified = modified;


    }

    public void checkPrivate(User userObj) {
        if (getPrivate_().equals(true)) {
            if (!userObj.equals(creator) && !getPrivateUsers().contains(userObj)) {
                throw new NoSuchElementException("Private discussion, not allowed to view");
            }
        }
    }

    public void checkBlocked(User userObj) {
        if (getBlockedUsers().contains(userObj)) {
            throw new NoSuchElementException("You have been blocked from this discussion");
        }
    }

    public static Discussion create(Model d,
                                    Tables.CommunityNoTextView cntv,
                                    List<Tables.DiscussionTagView> discussionTags,
                                    List<Tables.DiscussionUserView> discussionUsers,
                                    List<Tables.CommunityUserView> communityUsers,
                                    Integer vote) {
        // convert the tags
        List<Tag> tags = null;
        if (discussionTags != null) {
            tags = new ArrayList<>();
            for (Tables.DiscussionTagView dtv : discussionTags) {
                tags.add(Tag.create(dtv.getLong("tag_id"), dtv.getString("name")));
            }
        }

        // convert the user discussion roles
        User creator = null;
        List<User> privateUsers = new ArrayList<>();
        List<User> blockedUsers = new ArrayList<>();

        if (discussionUsers != null) {
            for (Tables.DiscussionUserView udv : discussionUsers) {

                DiscussionRole role = DiscussionRole.values()[udv.getLong("discussion_role_id").intValue() - 1];

                User userObj = User.create(udv.getLong("user_id"), udv.getString("name"));

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

        // Create the community
        Community community = (cntv != null) ? Community.create(cntv, null, communityUsers, null) : null;

        // If the community is NSFW, the discussion must be
        Boolean nsfw = (community != null && community.getNsfw()) ? true :  d.getBoolean("nsfw");

        // Create the modified by user
        User modifiedByUser = User.create(d.getLong("modified_by_user_id"), d.getString("modified_by_user_name"));

        return new Discussion(d.getLongId(),
                d.getString("title"),
                d.getString("link"),
                d.getString("text_"),
                d.getBoolean("private"),
                nsfw,
                d.getBoolean("stickied"),
                d.getInteger("avg_rank"),
                vote,
                d.getInteger("number_of_votes"),
                d.getInteger("number_of_comments"),
                tags,
                creator,
                modifiedByUser,
                privateUsers,
                blockedUsers,
                d.getBoolean("deleted"),
                community,
                d.getTimestamp("created"),
                d.getTimestamp("modified"));
    }

    public static Discussion fromJson(String dataStr) {

        try {
            return Tools.JACKSON.readValue(dataStr, Discussion.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getText() {
        return ConstantsService.INSTANCE.replaceCensoredText(text);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return ConstantsService.INSTANCE.replaceCensoredText(title);
    }

    public String getLink() {
        return link;
    }

    public Boolean getPrivate_() {
        return private_;
    }

    public Boolean getNsfw() {
        return nsfw;
    }

    public Boolean getStickied() {
        return stickied;
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

    public Integer getNumberOfComments() {
        return numberOfComments;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public List<User> getPrivateUsers() {
        return privateUsers;
    }

    public List<User> getBlockedUsers() {
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

    public User getCreator() {
        return creator;
    }

    public Community getCommunity() {
        return community;
    }

    public User getModifiedByUser() {
        return modifiedByUser;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Discussion that = (Discussion) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
