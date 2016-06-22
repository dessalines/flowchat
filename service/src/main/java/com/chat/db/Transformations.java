package com.chat.db;

import ch.qos.logback.classic.Logger;
import com.chat.tools.Tools;
import com.chat.types.CommentObj;
import com.chat.types.DiscussionObj;
import com.chat.types.UserObj;
import org.javalite.activejdbc.LazyList;
import org.javalite.activejdbc.Model;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.*;

import static com.chat.db.Tables.*;

/**
 * Created by tyler on 5/27/16.
 */
public class Transformations {

    public static Logger log = (Logger) LoggerFactory.getLogger(Transformations.class);


    public static CommentObj convertCommentThreadedView(Model cv, Integer vote) {
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
                cv.getInteger("avg_rank"),
                vote,
                cv.getInteger("number_of_votes"),
                cv.getTimestamp("created"),
                cv.getTimestamp("modified"));
    }



    public static Map<Long, CommentObj> convertCommentThreadedViewToMap(List<? extends Model> cvs,
                                                                        Map<Long, Integer> votes) {

        // Create a top level map of ids to comments
        Map<Long, CommentObj> commentObjMap = new LinkedHashMap<>();

        for (Model cv : cvs) {

            Long id = cv.getLong("id");

            // Check to make sure it has a vote
            Integer vote = (votes != null && votes.containsKey(id)) ? votes.get(id) : null;

            // Create the comment object
            CommentObj co = convertCommentThreadedView(cv, vote);

            commentObjMap.put(id, co);
        }

        return commentObjMap;
    }

    public static List<CommentObj> convertCommentsMapToEmbeddedObjects(Map<Long, CommentObj> commentObjMap) {

        List<CommentObj> cos = new ArrayList<>();

        for (Map.Entry<Long, CommentObj> e : commentObjMap.entrySet()) {

            Long id = e.getKey();
            CommentObj co = e.getValue();

//            log.info(co.json());

            Long parentId = commentObjMap.get(id).getParentId();

            // If its top level, add it
            if (parentId == null || id == co.getTopParentId()) {
                cos.add(co);
            }
            else {
                // Get the immediate parent
                CommentObj parent = commentObjMap.get(parentId);

                // Add it to the embedded object
                parent.getEmbedded().add(co);

                Collections.sort(parent.getEmbedded(), new CommentObj.CommentObjComparator());

            }

        }

        Collections.sort(cos, new CommentObj.CommentObjComparator());

        return cos;
    }

    public static List<CommentObj> convertCommentsToEmbeddedObjects(List<? extends Model> cvs,
                                                                    Map<Long, Integer> votes) {

        Map<Long, CommentObj> commentObjMap = convertCommentThreadedViewToMap(cvs, votes);

        List<CommentObj> cos = convertCommentsMapToEmbeddedObjects(commentObjMap);

        return cos;
    }


    public static DiscussionObj convertDiscussion(Model d, Integer vote) {
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


    public static List<DiscussionObj> convertDiscussionsToObjects(LazyList<? extends Model> discussions,
                                                                          Map<Long, Integer> votes) {
        // Convert to a list of discussion objects
        List<DiscussionObj> dos = new ArrayList<>();

        for (Model view : discussions) {
            Long id = view.getLongId();
            Integer vote = (votes.get(id) != null) ? votes.get(id) : null;
            DiscussionObj df = Transformations.convertDiscussion(view, vote);
            dos.add(df);
        }

        return dos;
    }

    public static Map<Long, Integer> convertDiscussionRankToMap(Set<Long> discussionIds, UserObj userObj) {
        LazyList<DiscussionRank> drs = DiscussionRank.where(
                "discussion_id in ? and user_id = ?",
                Tools.convertListToInQuery(discussionIds),
                userObj.getId());

        // Convert those votes to a map from id to rank
        Map<Long, Integer> discussionRankMap = new HashMap<>();
        for (DiscussionRank dr : drs) {
            discussionRankMap.put(dr.getLongId(), dr.getInteger("rank"));
        }

        return discussionRankMap;
    }


}
