package com.chat.types.community;

import com.chat.db.Tables;
import com.chat.tools.Tools;
import com.chat.types.JSONWriter;
import com.chat.types.tag.Tag;
import com.chat.types.user.User;
import com.chat.webservice.ConstantsService;
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
public class Community implements JSONWriter {

    private Long id;
    private User creator, modifiedByUser;
    private String name, text;
    private Boolean private_, deleted, nsfw;
    private Integer avgRank, userRank, numberOfVotes;
    private List<Tag> tags;
    private List<User> moderators, privateUsers, blockedUsers;
    private Timestamp created, modified;

    public Community() {}

    public Community(Long id,
                     String name,
                     String text,
                     Boolean private_,
                     Boolean nsfw,
                     Integer avgRank,
                     Integer userRank,
                     Integer numberOfVotes,
                     List<Tag> tags,
                     User creator,
                     User modifiedByUser,
                     List<User> moderators,
                     List<User> privateUsers,
                     List<User> blockedUsers,
                     Boolean deleted,
                     Timestamp created,
                     Timestamp modified) {
        this.id = id;
        this.creator = creator;
        this.modifiedByUser = modifiedByUser;
        this.name = name;
        this.text = text;
        this.private_ = private_;
        this.nsfw = nsfw;
        this.avgRank = avgRank;
        this.userRank = userRank;
        this.numberOfVotes = numberOfVotes;
        this.tags = tags;
        this.creator = creator;
        this.moderators = moderators;
        this.privateUsers = privateUsers;
        this.blockedUsers = blockedUsers;
        this.deleted = deleted;
        this.created = created;
        this.modified = modified;


    }


    public void checkPrivate(User userObj) {
        if (getPrivate_().equals(true)) {
            if (!getCreator().equals(userObj) &&
                    !getModerators().contains(userObj) &&
                    !getPrivateUsers().contains(userObj)) {
                throw new NoSuchElementException("Private community, not allowed to view");
            }
        }
    }

    public void checkBlocked(User userObj) {
        if (getBlockedUsers().contains(userObj)) {
            throw new NoSuchElementException("You have been blocked from this community");
        }
    }



    public static Community create(Model c,
                                   List<Tables.CommunityTagView> communityTags,
                                   List<Tables.CommunityUserView> communityUsers,
                                   Integer vote) {
        // convert the tags
        List<Tag> tags = null;
        if (communityTags != null) {
            tags = new ArrayList<>();
            for (Tables.CommunityTagView dtv : communityTags) {
                tags.add(Tag.create(dtv.getLong("tag_id"), dtv.getString("name")));
            }
        }

        // convert the user community roles
        User creator = null;
        List<User> moderators = new ArrayList<>();
        List<User> privateUsers = new ArrayList<>();
        List<User> blockedUsers = new ArrayList<>();

        if (communityUsers != null) {
            for (Tables.CommunityUserView udv : communityUsers) {

                CommunityRole role = CommunityRole.values()[udv.getLong("community_role_id").intValue() - 1];

                User userObj = User.create(udv.getLong("user_id"), udv.getString("name"));

                switch (role) {
                    case CREATOR:
                        creator = userObj;
                        break;
                    case MODERATOR:
                        moderators.add(userObj);
                        break;
                    case BLOCKED:
                        blockedUsers.add(userObj);
                        break;
                    case USER:
                        privateUsers.add(userObj);
                        break;
                }
            }
        }

        // Create the modified by user
        User modifiedByUser = User.create(c.getLong("modified_by_user_id"), c.getString("modified_by_user_name"));

        return new Community(c.getLongId(),
                c.getString("name"),
                c.getString("text_"),
                c.getBoolean("private"),
                c.getBoolean("nsfw"),
                c.getInteger("avg_rank"),
                vote,
                c.getInteger("number_of_votes"),
                tags,
                creator,
                modifiedByUser,
                moderators,
                privateUsers,
                blockedUsers,
                c.getBoolean("deleted"),
                c.getTimestamp("created"),
                c.getTimestamp("modified"));
    }

    public static Community fromJson(String dataStr) {

        try {
            return Tools.JACKSON.readValue(dataStr, Community.class);
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

    public String getName() {
        return ConstantsService.INSTANCE.replaceCensoredText(name);
    }

    public List<User> getModerators() {return moderators;}

    public Boolean getPrivate_() {
        return private_;
    }

    public Boolean getNsfw() {
        return nsfw;
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

    public User getCreator() {return creator;}

    public User getModifiedByUser() {
        return modifiedByUser;
    }
}
