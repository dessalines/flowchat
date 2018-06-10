package com.chat.types.comment;

import com.chat.tools.Tools;
import com.chat.types.JSONWriter;
import com.chat.types.RankingConstants;
import com.chat.types.user.User;
import com.chat.webservice.ConstantsService;
import org.javalite.activejdbc.Model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by tyler on 6/7/16.
 */
public class Comment implements JSONWriter {
    private Long id, discussionId, discussionOwnerId, parentId, topParentId, parentUserId, pathLength, numOfParents,
            numOfChildren;
    private String text;
    private Timestamp created, modified;
    private List<Comment> embedded;
    private List<Long> breadcrumbs;
    private Integer avgRank, userRank, numberOfVotes;
    private Boolean deleted, read, stickied;

    private User user, modifiedByUser;

    public Comment(Long id, User user, User modifiedByUser, Long discussionId, Long discussionOwnerId, String text,
            Long pathLength, Long topParentId, Long parentUserId, String breadcrumbs, Long numOfParents,
            Long numOfChildren, Integer avgRank, Integer userRank, Integer numberOfVotes, Boolean deleted, Boolean read,
            Boolean stickied, Timestamp created, Timestamp modified) {
        this.id = id;
        this.user = user;
        this.modifiedByUser = modifiedByUser;
        this.topParentId = topParentId;
        this.parentUserId = parentUserId;
        this.text = text;
        this.discussionId = discussionId;
        this.discussionOwnerId = discussionOwnerId;
        this.numOfParents = numOfParents;
        this.numOfChildren = numOfChildren;
        this.avgRank = avgRank;
        this.userRank = userRank;
        this.pathLength = pathLength;
        this.created = created;
        this.modified = modified;
        this.numberOfVotes = numberOfVotes;
        this.deleted = deleted;
        this.read = read;
        this.stickied = stickied;

        this.embedded = new ArrayList<>();

        this.breadcrumbs = setBreadCrumbsArr(breadcrumbs);
        setParentId();

    }

    public static Comment create(Model cv, Integer vote) {

        User user = User.create(cv.getLong("user_id"), cv.getString("user_name"));
        User modifiedByUser = User.create(cv.getLong("modified_by_user_id"), cv.getString("modified_by_user_name"));

        return new Comment(cv.getLong("id"), user, modifiedByUser, cv.getLong("discussion_id"),
                cv.getLong("discussion_owner_id"), cv.getString("text_"), cv.getLong("path_length"),
                cv.getLong("parent_id"), cv.getLong("parent_user_id"), cv.getString("breadcrumbs"),
                cv.getLong("num_of_parents"), cv.getLong("num_of_children"), cv.getInteger("avg_rank"), vote,
                cv.getInteger("number_of_votes"), cv.getBoolean("deleted"), cv.getBoolean("read"),
                cv.getBoolean("stickied"), cv.getTimestamp("created"), cv.getTimestamp("modified"));
    }

    public static List<Long> setBreadCrumbsArr(String breadCrumbs) {
        List<Long> breadcrumbs = new ArrayList<>();
        for (String br : Tools.pgArrayAggToArray(breadCrumbs)) {
            breadcrumbs.add(Long.valueOf(br.replace("\"", "")));
        }
        return breadcrumbs;
    }

    private void setParentId() {
        Integer cIndex = breadcrumbs.indexOf(id);

        if (cIndex > 0) {
            parentId = breadcrumbs.get(cIndex - 1);
        }

    }

    public static Comment findInEmbeddedById(List<Comment> cos, Comment co) {
        Long id = co.getParentId();

        for (Comment c : cos) {
            if (c.getId() == id) {
                return c;
            }
        }

        return co;

    }

    public static class CommentObjComparatorHot implements Comparator<Comment> {

        @Override
        public int compare(Comment o1, Comment o2) {

            Integer stickyComp = stickyComparison(o1, o2);
            if (stickyComp != 0) {
                return stickyComp;
            }

            Double o1R = getRank(o1);
            Double o2R = getRank(o2);

            return o2R.compareTo(o1R);
        }

        private static Double getRank(Comment co) {

            RankingConstants rco = ConstantsService.INSTANCE.getRankingConstants();

            Double timeDifference = (new Date().getTime() - co.getCreated().getTime()) * 0.001;
            Double timeRank = rco.getCreatedWeight() / timeDifference;
            Double numberOfVotesRank = (co.getNumberOfVotes() != null)
                    ? co.getNumberOfVotes() * rco.getNumberOfVotesWeight()
                    : 0;
            Double avgScoreRank = (co.getAvgRank() != null) ? co.getAvgRank() * rco.getAvgRankWeight() : 0;
            Double rank = timeRank + numberOfVotesRank + avgScoreRank;

            return rank;

        }

    }

    public static class CommentObjComparatorNew implements Comparator<Comment> {
        @Override
        public int compare(Comment o1, Comment o2) {

            Integer stickyComp = stickyComparison(o1, o2);
            if (stickyComp != 0) {
                return stickyComp;
            }

            return o2.getCreated().compareTo(o1.getCreated());
        }
    }

    public static class CommentObjComparatorTop implements Comparator<Comment> {

        @Override
        public int compare(Comment o1, Comment o2) {

            Integer stickyComp = stickyComparison(o1, o2);
            if (stickyComp != 0) {
                return stickyComp;
            }


            Integer o1R = (o1.getAvgRank() != null) ? o1.getAvgRank() : 50;
            Integer o2R = (o2.getAvgRank() != null) ? o2.getAvgRank() : 50;

            return o2R.compareTo(o1R);
        }

    }

    public static Integer stickyComparison(Comment o1, Comment o2) {
        return o2.getStickied().compareTo(o1.getStickied());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Comment that = (Comment) o;

        if (!id.equals(that.id))
            return false;
        if (user.getId() != null ? !user.getId().equals(that.user.getId()) : that.user.getId() != null)
            return false;
        if (discussionId != null ? !discussionId.equals(that.discussionId) : that.discussionId != null)
            return false;

        return read != null ? read.equals(that.read) : that.read == null;

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (user.getId() != null ? user.getId().hashCode() : 0);
        result = 31 * result + (discussionId != null ? discussionId.hashCode() : 0);
        return result;
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

    public Long getNumOfChildren() {
        return numOfChildren;
    }

    public Long getId() {
        return id;
    }

    public Long getDiscussionId() {
        return discussionId;
    }

    public Long getDiscussionOwnerId() {
        return discussionOwnerId;
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
        return ConstantsService.INSTANCE.replaceCensoredText(text);
    }

    public Boolean getStickied() {
        return stickied;
    }

    public Timestamp getCreated() {
        return created;
    }

    public Timestamp getModified() {
        return modified;
    }

    public List<Comment> getEmbedded() {
        return embedded;
    }

    public List<Long> getBreadcrumbs() {
        return breadcrumbs;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public Long getParentUserId() {
        return parentUserId;
    }

    public Boolean getRead() {
        return read;
    }

    public User getUser() {
        return user;
    }

    public User getModifiedByUser() {
        return modifiedByUser;
    }

}
