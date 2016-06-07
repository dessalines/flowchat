package com.chat.types;

import com.chat.tools.Tools;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;

/**
 * Created by tyler on 6/7/16.
 */
public interface JSONWriter {
    default String json() {
        try {
            return Tools.JACKSON.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

}
