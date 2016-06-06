package com.chat.db;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.*;

import static com.chat.db.Tables.*;

/**
 * Created by tyler on 5/27/16.
 */
public class Transformations {

    public static Logger log = (Logger) LoggerFactory.getLogger(Transformations.class);

    public static class CommentObj {
        private Long id, userId, discussionId, parentId, topParentId, pathLength, numOfParents, numOfChildren;
        private String userName, text;
        private Timestamp created;
        private List<CommentObj> embedded;
        private List<Long> breadcrumbs;

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
                          Timestamp created
                          ) {
            this.id = id;
            this.userId = userId;
            this.userName = userName;
            this.topParentId = topParentId;
            this.text = text;
            this.discussionId = discussionId;
            this.numOfChildren = numOfChildren;
            this.numOfParents = numOfParents;
            this.pathLength = pathLength;
            this.created = created;

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

        @Override
        public String toString() {
            return "{" +
                    "\"id\":" + id +
                    ", \"user_id\":" + userId +
                    ", \"user_name\":\"" + userName + "\"" +
                    ", \"discussionId\":" + discussionId +
                    ", \"parentId\":" + parentId +
                    ", \"topParentId\":" + topParentId +
                    ", \"pathLength\":" + pathLength +
                    ", \"numOfParents\":" + numOfParents +
                    ", \"numOfChildren\":" + numOfChildren +
                    ", \"text\":\"" + text + "\"" +
                    ", \"created\":\"" + created + "\"" +
                    ", \"embedded\":" + embedded +
                    ", \"breadcrumbs\":\"" + breadcrumbs + "\"" +
                    "}";
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

        public List<CommentObj> getEmbedded() {
            return embedded;
        }

        public List<Long> getBreadcrumbs() {
            return breadcrumbs;
        }

        public Long getUserId() { return userId; }

        public String getUserName() { return userName;}
    }


    public static Map<Long, CommentObj> convertCommentThreadedViewToMap(List<CommentThreadedView> cvs) {

        // Create a top level map of ids to comments
        Map<Long, CommentObj> commentObjMap = new LinkedHashMap<>();

        for (CommentThreadedView cv : cvs) {

            Long id = cv.getLong("id");

            // Create the comment object
            CommentObj co = new CommentObj(cv.getLong("id"),
                    cv.getLong("user_id"),
                    cv.getString("user_name"),
                    cv.getLong("discussion_id"),
                    cv.getString("text_"),
                    cv.getLong("path_length"),
                    cv.getLong("parent_id"),
                    cv.getString("breadcrumbs"),
                    cv.getLong("num_of_parents"),
                    cv.getLong("num_of_children"),
                    cv.getTimestamp("created"));

            commentObjMap.put(id, co);
        }

        return commentObjMap;
    }

    public static List<CommentObj> convertCommentsMapToEmbeddedObjects(Map<Long, CommentObj> commentObjMap) {

        List<CommentObj> cos = new ArrayList<>();

        for (Map.Entry<Long, CommentObj> e : commentObjMap.entrySet()) {

            Long id = e.getKey();
            CommentObj co = e.getValue();

            Long parentId = commentObjMap.get(id).getParentId();

            // If its top level, add it
            if (parentId == null) {
                cos.add(co);
            }
            else {
                // Get the immediate parent
                CommentObj parent = commentObjMap.get(parentId);

                // Add it to the embedded object
                parent.getEmbedded().add(co);

            }

        }

        return cos;
    }

    public static List<CommentObj> convertCommentsToEmbeddedObjects(List<CommentThreadedView> cvs) {

        Map<Long, CommentObj> commentObjMap = convertCommentThreadedViewToMap(cvs);

        List<CommentObj> cos = convertCommentsMapToEmbeddedObjects(commentObjMap);

        return cos;
    }

}
