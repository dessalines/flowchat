package com.chat.types;

import com.chat.db.Tables;
import org.eclipse.jetty.websocket.api.Session;

import java.util.*;

/**
 * Created by tyler on 6/7/16.
 */
public class Users implements JSONWriter {
    public Set<UserObj> users;

    public Users(Set<UserObj> users) {
        this.users = users;
    }

    public Set<UserObj> getUsers() {
        return users;
    }
}