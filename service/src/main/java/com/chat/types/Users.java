package com.chat.types;

import org.eclipse.jetty.websocket.api.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by tyler on 6/7/16.
 */
public class Users implements JSONWriter {
    public List<Long> users;

    public Users(Map<Session, Long> userMap) {
        this.users = new ArrayList<>(userMap.values());
    }

    public List<Long> getUsers() {
        return users;
    }
}