package com.chat.types;

import com.chat.db.Tables.Tag;

/**
 * Created by tyler on 6/19/16.
 */
public class TagObj implements JSONWriter {
    private Long id;
    private String name;

    private TagObj(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public TagObj() {}

    public static TagObj create(Tag tag) {
        return new TagObj(tag.getLongId(),
                tag.getString("name"));
    }

    public static TagObj create(Long id, String name) {
        return new TagObj(id, name);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TagObj tagObj = (TagObj) o;

        if (id != null ? !id.equals(tagObj.id) : tagObj.id != null) return false;
        return name != null ? name.equals(tagObj.name) : tagObj.name == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
