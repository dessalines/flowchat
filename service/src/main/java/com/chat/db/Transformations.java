package com.chat.db;

import ch.qos.logback.classic.Logger;
import com.chat.types.CommentObj;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.*;

import static com.chat.db.Tables.*;

/**
 * Created by tyler on 5/27/16.
 */
public class Transformations {

    public static Logger log = (Logger) LoggerFactory.getLogger(Transformations.class);


    public static CommentObj convertCommentThreadedView(CommentThreadedView cv) {
        return new CommentObj(cv.getLong("id"),
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
    }


    public static Map<Long, CommentObj> convertCommentThreadedViewToMap(List<CommentThreadedView> cvs) {

        // Create a top level map of ids to comments
        Map<Long, CommentObj> commentObjMap = new LinkedHashMap<>();

        for (CommentThreadedView cv : cvs) {

            Long id = cv.getLong("id");

            // Create the comment object
            CommentObj co = convertCommentThreadedView(cv);

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
