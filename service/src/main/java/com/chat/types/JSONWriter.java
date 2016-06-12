package com.chat.types;

import com.chat.tools.Tools;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;

/**
 * Created by tyler on 6/7/16.
 */
public interface JSONWriter {
    default String json(String wrappedName) {
        try {
            String val = Tools.JACKSON.writeValueAsString(this);

            String json = (wrappedName != null) ? "{\"" + wrappedName + "\":" +
                    val +
                    "}" : val;

            return json;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    default String json() {
        return json(null);
    }

}
