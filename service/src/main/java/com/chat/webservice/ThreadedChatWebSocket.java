package com.chat.webservice;

import com.chat.db.Actions;
import com.chat.db.Transformations;
import com.chat.tools.Tools;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.javalite.activejdbc.LazyList;

import java.io.IOException;
import java.sql.Array;
import org.postgresql.jdbc.PgArray;
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
    static Long nextUserNumber;

    // The comment rows
    static LazyList<CommentThreadedView> comments;

    public ThreadedChatWebSocket() {
        Tools.dbInit();
        nextUserNumber = USER.findAll().orderBy("id desc").limit(1).get(0).getLong("id") + 1;

        comments = fetchComments();

        new FullData(Transformations.convertCommentsToEmbeddedObjects(comments),
                new ArrayList<>(userMap.values()));
        Tools.dbClose();
    }


    @OnWebSocketConnect
    public void onConnect(Session user) throws Exception {

        Long userId = userMap.get(user);

        if (userId == null) {
            // Create the user if necessary
            Tools.dbInit();
            User dbUser = Actions.createUser();
            userId = dbUser.getLongId();
            Tools.dbClose();

            userMap.put(user, userId);
        }

        // Send the comments to them
        user.getRemote().sendString(convertToCommentsJson());

    }

    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {
        Long userId = userMap.get(user);
        userMap.remove(user);

        log.info("user " + userId + " left, " + statusCode + " " + reason);
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

        Actions.createComment(userMap.get(user), 1L, parentBreadCrumbs, reply.getReply());

        comments = fetchComments();

        broadcastMessage(userMap.get(user), convertToCommentsJson());

        // TODO either fetch all the data *bad*, or just add that row to the lazylist

        Tools.dbClose();


    }

    //Sends a message from one user to all users, along with a list of current usernames
    public static void broadcastMessage(Long userId, String json) {
        userMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
            try {
                session.getRemote().sendString(json);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static class Reply {
        private Long parentId;
        private String reply;

        public Reply(Long parentId, String reply) {
            this.parentId = parentId;
            this.reply = reply;
        }

        public Reply() {}

        public Long getParentId() {
            return parentId;
        }

        public String getReply() {
            return reply;
        }

        private static Reply fromJson(String replyDataStr) {

            try {
                return Tools.JACKSON.readValue(replyDataStr, Reply.class);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    private static LazyList<CommentThreadedView> fetchComments() {
        return COMMENT_THREADED_VIEW.where("discussion_id = ?", 1);
    }

    public static String convertToCommentsJson() {
        return new FullData(Transformations.convertCommentsToEmbeddedObjects(comments),
                new ArrayList<>(userMap.values())).json();
    }

    private static class FullData {
        private List<CommentObj> comments;
        private List<Long> users;

        public FullData(List<CommentObj> comments, List<Long> users) {
            this.comments = comments;
            this.users = users;
        }

        public String json() {
            try {
                return Tools.JACKSON.writeValueAsString(this);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return null;
        }

        public List<Long> getUsers() {
            return users;
        }

        public List<CommentObj> getComments() {
            return comments;
        }
    }


}
