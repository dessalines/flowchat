package com.chat.types;

import com.chat.tools.Tools;
import org.javalite.activejdbc.Model;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tyler on 6/19/16.
 */
public class DiscussionObj implements JSONWriter {
    private Long id, userId;
    private String userName, title, link, text;
    private Boolean private_;
    private Integer avgRank, userRank, numberOfVotes;
    private List<TagObj> tags;
    private Timestamp created, modified;

    public DiscussionObj() {}

    public DiscussionObj(Long id,
                         Long userId,
                         String userName,
                         String title,
                         String link,
                         String text,
                         Boolean private_,
                         Integer avgRank,
                         Integer userRank,
                         Integer numberOfVotes,
                         String tagIds,
                         String tagNames,
                         Timestamp created,
                         Timestamp modified) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.title = title;
        this.link = link;
        this.text = text;
        this.private_ = private_;
        this.avgRank = avgRank;
        this.userRank = userRank;
        this.numberOfVotes = numberOfVotes;
        this.tags = (!tagIds.equals("{NULL}")) ? setTags(tagIds, tagNames) : null;
        this.created = created;
        this.modified = modified;
    }

    public static DiscussionObj create(Model d, Integer vote) {
        return new DiscussionObj(d.getLongId(),
                d.getLong("user_id"),
                d.getString("user_name"),
                d.getString("title"),
                d.getString("link"),
                d.getString("text_"),
                d.getBoolean("private"),
                d.getInteger("avg_rank"),
                vote,
                d.getInteger("number_of_votes"),
                d.getString("tag_ids"),
                d.getString("tag_names"),
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
            tags.add(new TagObj(Long.valueOf(ids[i]), names[i]));
        }

        return tags;
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

    public String getUserName() {return userName;}

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

    public Timestamp getCreated() {
        return created;
    }

    public Timestamp getModified() {
        return modified;
    }
}
