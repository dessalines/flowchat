package com.chat.db;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import ch.qos.logback.classic.Logger;
import com.chat.db.Tables.*;
import org.slf4j.LoggerFactory;

import static com.chat.db.Tables.*;
/**
 * Created by tyler on 6/5/16.
 */
public class Actions {

    public static Logger log = (Logger) LoggerFactory.getLogger(Actions.class);


    public static String createComment(Long userId, Long discussionId,
                                       List<Long> parentBreadCrumbs, String text) {

        List<Long> pbs = (parentBreadCrumbs != null) ? new ArrayList<Long>(parentBreadCrumbs) :
                new ArrayList<Long>();


        // find the candidate
        Comment c = COMMENT.createIt("discussion_id", discussionId,
                "text_", text,
                "user_id", userId);


        Long childId = c.getLong("id");

        // This is necessary, because of the 0 path length to itself one
        pbs.add(childId);

        Collections.reverse(pbs);


        // Create the comment_tree
        for (int i = 0; i < pbs.size(); i++) {

            Long parentId = pbs.get(i);

            // i is the path length
            COMMENT_TREE.createIt("parent_id", parentId,
                    "child_id", childId,
                    "path_length", i);
        }

        return "Comment created";

    }

    public static User createUser() {
        User user = USER.createIt(
                "name", "asdf");

        user.set("name", "user_" + user.getLongId()).saveIt();

        return user;

    }
}
