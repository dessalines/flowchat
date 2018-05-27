package com.chat.types.user;

import com.chat.types.JSONWriter;
import org.javalite.activejdbc.LazyList;

import java.util.*;

/**
 * Created by tyler on 6/7/16.
 */
public class Users implements JSONWriter {
    public Set<User> users;

    private Users(Set<User> users) {
        this.users = users;
    }

    public static Users create(LazyList<com.chat.db.Tables.User> users) {
        // Convert to a list of discussion objects
        Set<User> uos = new LinkedHashSet<>();

        for (com.chat.db.Tables.User user : users) {
            User uo = User.create(user);
            uos.add(uo);
        }

        return new Users(uos);
    }

    public static Users create(Set<User> users) {
        return new Users(users);
    }

    public Set<User> getUsers() {
        return users;
    }
}