package com.chat.types;

/**
 * Created by tyler on 6/10/16.
 */
public class UserObj implements JSONWriter {
    private Long id;
    private String name;

    public UserObj(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
