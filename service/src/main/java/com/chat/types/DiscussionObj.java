package com.chat.types;

import com.chat.db.Tables;
import com.chat.tools.Tools;
import org.javalite.activejdbc.LazyList;
import org.javalite.activejdbc.Model;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by tyler on 6/19/16.
 */
public class DiscussionObj implements JSONWriter {
    private Long id;
    private UserObj creator;
    private String title, link, text;
    private Boolean private_, deleted;
    private Integer avgRank, userRank, numberOfVotes;
    private List<TagObj> tags;
    private List<UserObj> privateUsers, blockedUsers;
    private Timestamp created, modified;

    public DiscussionObj() {
    }

    public DiscussionObj(Long id,
                         String title,
                         String link,
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
        this.title = title;
        this.link = link;
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

    public enum DISCUSSION_ROLE {
        CREATOR(1), USER(2), BLOCKED(3);

        private Integer num;

        DISCUSSION_ROLE(Integer num) {
            this.num = num;
        }

        public Integer getNum() {
            return num;
        }
    }

    public void checkPrivate(UserObj userObj) {
        if (getPrivate_().equals(true)) {
            if (!getPrivateUsers().contains(userObj)) {
                throw new NoSuchElementException("Private discussion, not allowed to view");
            }
        }
    }

    public void checkBlocked(UserObj userObj) {
        System.out.println(Arrays.toString(getBlockedUsers().toArray()));
        if (getBlockedUsers().contains(userObj)) {
            throw new NoSuchElementException("You have been blocked from this discussion");
        }
    }

    public static DiscussionObj create(Model d,
                                       LazyList<Tables.DiscussionTagView> discussionTags,
                                       LazyList<Tables.UserDiscussionView> userDiscussions,
                                       Integer vote) {

        List<TagObj> tags = new ArrayList<>();
        for (Tables.DiscussionTagView dtv : discussionTags) {
            tags.add(TagObj.create(dtv.getLongId(), dtv.getString("name")));
        }
        UserObj creator = null;
        List<UserObj> privateUsers = new ArrayList<>();
        List<UserObj> blockedUsers = new ArrayList<>();

        for (Tables.UserDiscussionView udv : userDiscussions) {

            DISCUSSION_ROLE dr = udv.getLong("discussion_role_id").intValue();

            switch(udv.getLong("discussion_role_id").intValue()) {
                case DISCUSSION_ROLE.BLOCKED.getNum().intValue():
                    blockedUsers.add(UserObj.create(udv.getLong("user_id"), udv.getString("name")));
                    break;
                case DISCUSSION_ROLE.USER.getNum():
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

    public static List<TagObj> setTags(String tagIds, String tagNames) {
        List<TagObj> tags = new ArrayList<>();
        String[] ids = Tools.pgArrayAggToArray(tagIds);
        String[] names = Tools.pgArrayAggToArray(tagNames);

        for (int i = 0; i < ids.length; i++) {
            tags.add(TagObj.create(Long.valueOf(ids[i]), names[i]));
        }

        List<TagObj> dedupeTagObjs = new ArrayList<>(new LinkedHashSet<>(tags));

        return dedupeTagObjs;
    }

    public String getText() {
        return text;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
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


}
