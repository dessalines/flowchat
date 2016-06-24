package com.chat.types;

import com.chat.db.Tables.Tag;
import com.chat.db.Transformations;
import org.javalite.activejdbc.LazyList;
import org.javalite.activejdbc.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by tyler on 6/23/16.
 */
public class Tags implements JSONWriter {
    private List<TagObj> tags;

    private Tags(List<TagObj> tags) {
        this.tags = tags;
    }

    public static Tags create(LazyList<Tag> tags) {
        // Convert to a list of discussion objects
        List<TagObj> tos = new ArrayList<>();

        for (Tag tag : tags) {
            TagObj to = TagObj.create(tag);
            tos.add(to);
        }

        return new Tags(tos);
    }

    public List<TagObj> getTags() {
        return tags;
    }
}
