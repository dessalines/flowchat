package com.chat.types;

import com.chat.db.Tables.User;

/**
 * Created by tyler on 6/10/16.
 */
public class UserObj implements JSONWriter {
    private Long id;
    private String name;

    private UserObj(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public UserObj() {}

    public static UserObj create(User user) {
        return new UserObj(user.getLongId(),
                user.getString("name"));
    }

    public static UserObj create(Long id, String name) {
        return new UserObj(id, name);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return this.json();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserObj userObj = (UserObj) o;

        if (id != null ? !id.equals(userObj.id) : userObj.id != null) return false;
        return name != null ? name.equals(userObj.name) : userObj.name == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
