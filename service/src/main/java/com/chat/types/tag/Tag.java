package com.chat.types.tag;

import com.chat.types.JSONWriter;
import com.chat.webservice.ConstantsService;

/**
 * Created by tyler on 6/19/16.
 */
public class Tag implements JSONWriter {
    private Long id;
    private String name;

    private Tag(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Tag() {}

    public static Tag create(com.chat.db.Tables.Tag tag) {
        return new Tag(tag.getLongId(),
                tag.getString("name"));
    }

    public static Tag create(Long id, String name) {
        return new Tag(id, name);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return ConstantsService.INSTANCE.replaceCensoredText(name);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tag tagObj = (Tag) o;

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
