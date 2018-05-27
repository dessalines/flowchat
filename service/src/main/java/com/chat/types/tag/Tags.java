package com.chat.types.tag;

import com.chat.types.JSONWriter;
import org.javalite.activejdbc.LazyList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tyler on 6/23/16.
 */
public class Tags implements JSONWriter {
    private List<Tag> tags;

    private Tags(List<Tag> tags) {
        this.tags = tags;
    }

    public static Tags create(LazyList<com.chat.db.Tables.Tag> tags) {
        // Convert to a list of discussion objects
        List<Tag> tos = new ArrayList<>();

        for (com.chat.db.Tables.Tag tag : tags) {
            Tag to = Tag.create(tag);
            tos.add(to);
        }

        return new Tags(tos);
    }

    public List<Tag> getTags() {
        return tags;
    }
}
