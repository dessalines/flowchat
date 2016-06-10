package com.chat.webservice;

import com.chat.db.Actions;
import com.chat.db.Transformations;
import com.chat.tools.Tools;
import com.chat.types.*;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.javalite.activejdbc.LazyList;

import java.sql.Array;
import java.util.*;

import static com.chat.db.Tables.*;
import static com.chat.db.Transformations.*;

/**
 * Created by tyler on 6/5/16.
 */

@WebSocket
public class ThreadedChatWebSocket {

    private String sender, msg;

    static Map<Session, Long> userMap = new HashMap<>();

    // The comment rows
    static LazyList<CommentThreadedView> comments;

    public ThreadedChatWebSocket() {
        Tools.dbInit();

        comments = fetchComments();

        Tools.dbClose();
    }


    @OnWebSocketConnect
    public void onConnect(Session user) throws Exception {

        Tools.dbInit();
        User dbUser = setupUser(user);

        // Send them their user info
        user.getRemote().sendString(dbUser.toJson(false));

        // Send all the comments to just them
        user.getRemote().sendString(new Comments(comments).json());

        // Send the updated users to everyone
        broadcastMessage(userMap.get(user), new Users(userMap).json());

        Tools.dbClose();


    }

    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {
        Long userId = userMap.get(user);
        userMap.remove(user);

        log.info("user " + userId + " left, " + statusCode + " " + reason);

        // Send the updated users to everyone
        broadcastMessage(userMap.get(user), new Users(userMap).json());
    }

    @OnWebSocketMessage
    public void onMessage(Session user, String replyDataStr) {


        // Get the object
        Reply reply = Reply.fromJson(replyDataStr);

        // Save the data
        Tools.dbInit();

        // Collect only works on refetch
        comments = fetchComments();

        // Necessary for comment tree
        Array arr = (Array) comments.collect("breadcrumbs", "id", reply.getParentId()).get(0);

        List<Long> parentBreadCrumbs = Tools.convertArrayToList(arr);

        Comment newComment = Actions.createComment(userMap.get(user), 1L, parentBreadCrumbs, reply.getReply());

        // Fetch the comment threaded view
        CommentThreadedView ctv = COMMENT_THREADED_VIEW.findFirst("id = ?", newComment.getLongId());

        // Add it to the current lazy list
        comments.add(ctv);


        // Convert to a proper commentObj
        CommentObj co = Transformations.convertCommentThreadedView(ctv);


//        comments = fetchComments();


        broadcastMessage(userMap.get(user), co.json());

        Tools.dbClose();


    }

    //Sends a message from one user to all users
    public static void broadcastMessage(Long userId, String json) {
        userMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
            try {
                session.getRemote().sendString(json);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


    private User setupUser(Session user) {
        Long userId = userMap.get(user);


        if (userId == null) {
            // Create the user if necessary

            User dbUser = Actions.createUser();
            userId = dbUser.getLongId();


            userMap.put(user, userId);

            return dbUser;
        }

    }

    private static LazyList<CommentThreadedView> fetchComments() {
        return COMMENT_THREADED_VIEW.where("discussion_id = ?", 1);
    }









}
