package com.chat.types;

import com.chat.db.Tables;
import org.eclipse.jetty.websocket.api.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by tyler on 6/7/16.
 */
public class Users implements JSONWriter {
    public List<UserObj> users;

    public Users(Map<Session, UserObj> sessionToUserMap) {
        this.users = new ArrayList<>(sessionToUserMap.values());
    }

    public List<UserObj> getUsers() {
        return users;
    }
}