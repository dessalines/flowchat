package com.chat.types.websocket.input;

import com.chat.tools.Tools;
import com.chat.types.JSONWriter;

import java.io.IOException;

/**
 * Created by tyler on 6/25/16.
 */
public class DeleteData implements JSONWriter {
    private Long deleteId;

    public DeleteData(Long deleteId) {
        this.deleteId = deleteId;
    }

    public DeleteData() {}

    public Long getDeleteId() {
        return deleteId;
    }
    
    public static DeleteData fromJson(String deleteDataStr) {

        try {
            return Tools.JACKSON.readValue(deleteDataStr, DeleteData.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}