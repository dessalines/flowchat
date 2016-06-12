package com.chat.webservice;

import com.chat.db.Actions;
import com.chat.db.Transformations;
import com.chat.tools.Tools;
import com.chat.types.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.javalite.activejdbc.LazyList;

import java.io.IOException;
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

    static Map<Session, UserObj> sessionToUserMap = new HashMap<>();

    // The comment rows
    static LazyList<CommentThreadedView> comments;

    public ThreadedChatWebSocket() {
        Tools.dbInit();

        comments = fetchComments();

        Tools.dbClose();
    }


    @OnWebSocketConnect
    public void onConnect(Session session) throws Exception {

        Map<String, String> cookieMap = Tools.cookieListToMap(session.getUpgradeRequest().getCookies());

        Tools.dbInit();

        // Get or create the user
        UserObj userObj = setupUser(session, cookieMap.get("auth"));

        // Send them their user info
        session.getRemote().sendString(userObj.json());

        // Send all the comments to just them
        session.getRemote().sendString(new Comments(comments).json());

        // Send the updated users to everyone
        broadcastMessage(sessionToUserMap.get(session), new Users(sessionToUserMap).json());

        log.info("user " + userObj.getName() + " joined");


        Tools.dbClose();


    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {

        UserObj userObj = sessionToUserMap.get(session);
        sessionToUserMap.remove(session);

        log.info("user " + userObj.getId() + " left, " + statusCode + " " + reason);

        // Send the updated users to everyone
        broadcastMessage(sessionToUserMap.get(session), new Users(sessionToUserMap).json());
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String dataStr) {


        // Save the data
        Tools.dbInit();

        switch(getMessageType(dataStr)) {
            case Reply:
                messageReply(session, dataStr);
                break;
            case Edit:
                messageEdit(session, dataStr);
                break;
        }



        Tools.dbClose();


    }

    public MessageType getMessageType(String someData) {

        try {
        JsonNode rootNode = Tools.JACKSON.readTree(someData);

            Iterator<String> it = rootNode.fieldNames();

            while (it.hasNext()) {
                String nodeName = it.next();
                switch(nodeName) {
                    case "reply" :
                        return MessageType.Reply;
                    case "edit":
                        return MessageType.Edit;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    enum MessageType {
        Edit, Reply;
    }

    public void messageReply(Session session, String replyDataStr) {

        // Get the object
        ReplyData replyData = ReplyData.fromJson(replyDataStr);

        // Collect only works on refetch
        comments = fetchComments();

        // Necessary for comment tree
        Array arr = (Array) comments.collect("breadcrumbs", "id", replyData.getParentId()).get(0);

        List<Long> parentBreadCrumbs = Tools.convertArrayToList(arr);

        Comment newComment = Actions.createComment(sessionToUserMap.get(session).getId(),
                1L,
                parentBreadCrumbs,
                replyData.getReply());

        // Fetch the comment threaded view
        CommentThreadedView ctv = COMMENT_THREADED_VIEW.findFirst("id = ?", newComment.getLongId());

        // Add it to the current lazy list
        comments.add(ctv);


        // Convert to a proper commentObj
        CommentObj co = Transformations.convertCommentThreadedView(ctv);


//        comments = fetchComments();


        broadcastMessage(sessionToUserMap.get(session), co.json("reply"));

    }

    public void messageEdit(Session session, String editDataStr) {

        EditData editData = EditData.fromJson(editDataStr);

        Comment c = Actions.editComment(editData.getId(), editData.getEdit());

        // You need to get the breadcrumbs, since there could be many sub comments to this one
        List<CommentBreadcrumbsView> cbvs = COMMENT_BREADCRUMBS_VIEW.where("parent_id = ?", c.getLongId());

        // Convert to a proper commentObj
        CommentObj co = Transformations.convertCommentsToEmbeddedObjects(cbvs).get(0);
        log.info(co.json());

        // Refetch the comments
//        comments = fetchComments();

        // TODO A temp workaround until i find out a more generic way to do this
        CommentThreadedView ctv = COMMENT_THREADED_VIEW.findFirst("id = ?", c.getLongId());

        // Set the comment to its new value
        Integer index = Tools.findIndexByIdInLazyList(comments, c.getLongId());
        comments.set(index, ctv);

        // TODO not sure if this is going to work correctly
        broadcastMessage(sessionToUserMap.get(session), co.json("edit"));

    }

    //Sends a message from one user to all users
    public static void broadcastMessage(UserObj user, String json) {
        sessionToUserMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
            try {
                session.getRemote().sendString(json);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


    private UserObj setupUser(Session session, String auth) {

        UserObj userObj;

        if (auth != null) {
            UserLoginView uv = USER_LOGIN_VIEW.findFirst("auth = ?", auth);
            userObj = new UserObj(uv.getLongId(), uv.getString("name"));

        } else {
            User dbUser = Actions.createUser();
            userObj = new UserObj(dbUser.getLongId(), dbUser.getString("name"));
        }

        sessionToUserMap.put(session, userObj);

        return userObj;

    }

    private static LazyList<CommentThreadedView> fetchComments() {
        return COMMENT_THREADED_VIEW.where("discussion_id = ?", 1);
    }


}
