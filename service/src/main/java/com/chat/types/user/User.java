package com.chat.types.user;

import com.chat.db.Tables;
import com.chat.tools.Tools;
import com.chat.types.JSONWriter;

import java.io.IOException;

/**
 * Created by tyler on 6/10/16.
 */
public class User implements JSONWriter {
    private Long id;
    private String name, auth;

    private UserSettings settings;

    private User(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public User() {}

    public static User create(com.chat.db.Tables.User user) {
        return new User(user.getLongId(),
                user.getString("name"));
    }

    public static User create(Tables.UserView uv) {
        User user = new User(uv.getLongId(),
                uv.getString("name"));

        user.settings = UserSettings.create(uv);

        return user;
    }

    public static User create(Long id, String name) {
        return new User(id, name);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public UserSettings getSettings() {
        return settings;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    @Override
    public String toString() {
        return this.json();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User userObj = (User) o;

        if (id != null ? !id.equals(userObj.id) : userObj.id != null) return false;
        return name != null ? name.equals(userObj.name) : userObj.name == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    public static User fromJson(String dataStr) {

        try {
            return Tools.JACKSON.readValue(dataStr, User.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
