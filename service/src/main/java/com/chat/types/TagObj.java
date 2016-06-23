package com.chat.types;

import com.chat.db.Tables.Tag;

/**
 * Created by tyler on 6/19/16.
 */
public class TagObj implements JSONWriter {
    private Long id;
    private String name;

    public TagObj(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public TagObj() {}

    public static TagObj create(Tag tag) {
        return new TagObj(tag.getLongId(),
                tag.getString("name"));
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
