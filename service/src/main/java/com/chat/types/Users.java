package com.chat.types;

import com.chat.db.Tables.User;
import org.javalite.activejdbc.LazyList;

import java.util.*;

/**
 * Created by tyler on 6/7/16.
 */
public class Users implements JSONWriter {
    public Set<UserObj> users;

    private Users(Set<UserObj> users) {
        this.users = users;
    }

    public static Users create(LazyList<User> users) {
        // Convert to a list of discussion objects
        Set<UserObj> uos = new LinkedHashSet<>();

        for (User user : users) {
            UserObj uo = UserObj.create(user);
            uos.add(uo);
        }

        return new Users(uos);
    }

    public static Users create(Set<UserObj> users) {
        return new Users(users);
    }

    public Set<UserObj> getUsers() {
        return users;
    }
}