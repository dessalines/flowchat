package com.chat.types;


import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tyler on 6/7/16.
 */
public class CommentObj implements JSONWriter{
    private Long id, userId, discussionId, parentId, topParentId, pathLength, numOfParents, numOfChildren;
    private String userName, text;
    private Timestamp created, modified;
    private List<CommentObj> embedded;
    private List<Long> breadcrumbs;
    private Integer avgRank, userRank;

    public CommentObj(Long id,
                      Long userId,
                      String userName,
                      Long discussionId,
                      String text,
                      Long pathLength,
                      Long topParentId,
                      String breadcrumbs,
                      Long numOfParents,
                      Long numOfChildren,
                      Integer avgRank,
                      Integer userRank,
                      Timestamp created,
                      Timestamp modified
    ) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.topParentId = topParentId;
        this.text = text;
        this.discussionId = discussionId;
        this.numOfParents = numOfParents;
        this.numOfChildren = numOfChildren;
        this.avgRank = avgRank;
        this.userRank = userRank;
        this.pathLength = pathLength;
        this.created = created;
        this.modified = modified;

        this.embedded = new ArrayList<>();

        this.breadcrumbs = setBreadCrumbsArr(breadcrumbs);
        setParentId();

    }

    public static List<Long> setBreadCrumbsArr(String breadCrumbs) {
        List<Long> breadcrumbs = new ArrayList<>();
        for (String br : breadCrumbs.replaceAll("\\{|\\}", "").split(",")) {
            breadcrumbs.add(Long.valueOf(br));
        }
        return breadcrumbs;
    }

    private void setParentId() {
        Integer cIndex = breadcrumbs.indexOf(id);

        if (cIndex > 0) {
            parentId = breadcrumbs.get(cIndex - 1);
        }

    }


    public static CommentObj findInEmbeddedById(List<CommentObj> cos, CommentObj co) {
        Long id = co.getParentId();

        for (CommentObj c : cos) {
            if (c.getId() == id) {
                return c;
            }
        }

        return co;

    }

    public Integer getAvgRank() {
        return avgRank;
    }

    public Integer getUserRank() {
        return userRank;
    }


    public Long getNumOfChildren() {
        return numOfChildren;
    }

    public Long getId() {
        return id;
    }

    public Long getDiscussionId() {
        return discussionId;
    }

    public Long getParentId() {
        return parentId;
    }

    public Long getTopParentId() {
        return topParentId;
    }

    public Long getPathLength() {
        return pathLength;
    }

    public Long getNumOfParents() {
        return numOfParents;
    }

    public String getText() {
        return text;
    }

    public Timestamp getCreated() {
        return created;
    }

    public Timestamp getModified() {
        return modified;
    }


    public List<CommentObj> getEmbedded() {
        return embedded;
    }

    public List<Long> getBreadcrumbs() {
        return breadcrumbs;
    }

    public Long getUserId() { return userId; }

    public String getUserName() { return userName;}
}
