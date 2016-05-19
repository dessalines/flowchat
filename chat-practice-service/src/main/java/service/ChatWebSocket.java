package service;

import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

@WebSocket
public class ChatWebSocket {

    private String sender, msg;

    @OnWebSocketConnect
    public void onConnect(Session user) throws Exception {
        String username = "User" + ChatService.nextUserNumber++;
        ChatService.userNameMap.put(user, username);
        ChatService.broadcastMessage("Server", username + " joined the chat");
    }

    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {
        String username = ChatService.userNameMap.get(user);
        ChatService.userNameMap.remove(user);
        ChatService.broadcastMessage("Server", username + " left the chat");
    }

    @OnWebSocketMessage
    public void onMessage(Session user, String message) {
        ChatService.broadcastMessage(ChatService.userNameMap.get(user), message);
    }
}

