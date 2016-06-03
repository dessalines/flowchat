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
        private Integer id, discussionId, parentId, topParentId, pathLength, numOfParents, numOfChildren;
        private String text;
        private Timestamp created;
        private List<CommentObj> embedded;
        private List<Integer> breadcrumbs;

        public CommentObj(Integer id,
                          Integer discussionId,
                          String text,
                          Integer pathLength,
                          Integer topParentId,
                          String breadcrumbs,
                          Integer numOfParents,
                          Integer numOfChildren,
                          Timestamp created
                          ) {
            this.id = id;
            this.topParentId = topParentId;
            this.text = text;
            this.discussionId = discussionId;
            this.numOfChildren = numOfChildren;
            this.numOfParents = numOfParents;
            this.pathLength = pathLength;
            this.created = created;

            this.embedded = new ArrayList<>();

            setBreadCrumbsArr(breadcrumbs);
            setParentId();

        }

        private void setBreadCrumbsArr(String breadCrumbs) {
            breadcrumbs = new ArrayList<>();
            for (String br : breadCrumbs.replaceAll("\\{|\\}", "").split(",")) {
                breadcrumbs.add(Integer.valueOf(br));
            }
        }

        private void setParentId() {
            Integer cIndex = breadcrumbs.indexOf(id);

            if (cIndex > 0) {
                parentId = breadcrumbs.get(cIndex - 1);
            }

        }


        public static CommentObj findInEmbeddedById(List<CommentObj> cos, CommentObj co) {
            Integer id = co.getParentId();

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

        public Integer getNumOfChildren() {
            return numOfChildren;
        }

        public Integer getId() {
            return id;
        }

        public Integer getDiscussionId() {
            return discussionId;
        }

        public Integer getParentId() {
            return parentId;
        }

        public Integer getTopParentId() {
            return topParentId;
        }

        public Integer getPathLength() {
            return pathLength;
        }

        public Integer getNumOfParents() {
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

        public List<Integer> getBreadcrumbs() {
            return breadcrumbs;
        }
    }


    public static List<CommentObj> convertCommentsToEmbeddedObjects(List<CommentThreadedView> cvs) {
        List<CommentObj> cos = new ArrayList<>();

        // create a top level map of ids to comments
        Map<Integer, CommentObj> commentObjMap = new LinkedHashMap<>();

        for (CommentThreadedView cv : cvs) {

            Integer id = cv.getInteger("id");

            // Create the comment object
            CommentObj co = new CommentObj(cv.getInteger("id"),
                    cv.getInteger("discussion_id"),
                    cv.getString("text_"),
                    cv.getInteger("path_length"),
                    cv.getInteger("parent_id"),
                    cv.getString("breadcrumbs"),
                    cv.getInteger("num_of_parents"),
                    cv.getInteger("num_of_children"),
                    cv.getTimestamp("created"));

            commentObjMap.put(id, co);
        }


        for (Map.Entry<Integer, CommentObj> e : commentObjMap.entrySet()) {
            int cCos = 0;

            Integer id = e.getKey();
            CommentObj co = e.getValue();

            Integer parentId = commentObjMap.get(id).getParentId();

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

}
