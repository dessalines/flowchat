package com.chat.db;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
            log.info(breadCrumbs);
            breadcrumbs = new ArrayList<>();
            for (String br : breadCrumbs.split(",")) {
                breadcrumbs.add(Integer.valueOf(br));
            }
        }

        private void setParentId() {
            Integer cIndex = breadcrumbs.indexOf(id);

            if (cIndex > 0) {
                parentId = breadcrumbs.get(cIndex - 1);
            }

        }
    }


    public static List<CommentObj> convertCommentsToEmbeddedObjects(List<CommentThreadedView> cvs) {
        List<CommentObj> cos = new ArrayList<>();
        for (CommentThreadedView cv : cvs) {

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

            }


        return cos;
    }

}
