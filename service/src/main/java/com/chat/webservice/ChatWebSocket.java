package com.chat.webservice;

import com.chat.db.Tables.*;
import com.chat.tools.Tools;
import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.text.SimpleDateFormat;
import java.util.*;
import static com.chat.db.Tables.*;

@WebSocket
public class ChatWebSocket {

    private String sender, msg;

    static Map<Session, String> userNameMap = new HashMap<>();
    static Integer nextUserNumber;

    public ChatWebSocket() {
        // Fetch the last user(assume all new users for right now)
        Tools.dbInit();
        nextUserNumber = User.findAll().orderBy("id desc").limit(1).get(0).getInteger("id") + 1;
        Tools.dbClose();
    }

    @OnWebSocketConnect
    public void onConnect(Session user) throws Exception {
        String username = "User" + nextUserNumber++;
        userNameMap.put(user, username);
        broadcastMessage("Server", username + " joined the chat");
    }

    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {
        String username = userNameMap.get(user);
        userNameMap.remove(user);
        broadcastMessage("Server", username + " left the chat");
    }

    @OnWebSocketMessage
    public void onMessage(Session user, String message) {
        broadcastMessage(userNameMap.get(user), message);
    }

    //Sends a message from one user to all users, along with a list of current usernames
    public static void broadcastMessage(String sender, String message) {
        userNameMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
            try {
                FullData fd = new FullData(new Message(sender, message), new ArrayList<>(userNameMap.values()));
                String json = Tools.JACKSON.writeValueAsString(fd);
                System.out.println(json);
                session.getRemote().sendString(json);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static class Message {
        public String sender, message, time;

        public Message(String sender, String message) {
            this.sender = sender;
            this.message = message;
            this.time = new SimpleDateFormat("HH:mm:ss").format(new Date());
        }

    }

    public static class FullData {
        public Message userMessage;
        public List<String> userList;

        public FullData(Message userMessage, List<String> userList) {
            this.userMessage = userMessage;
            this.userList = userList;
        }
    }
}

