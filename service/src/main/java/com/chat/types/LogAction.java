package com.chat.types;

/**
 * Created by tyler on 8/17/16.
 */
public enum LogAction {

    DELETED(1), RESTORED(2), BLOCKED(3), UNBLOCKED(4), FAVORITED(5), UNFAVORITED(6);

    int val;
    LogAction(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }
}
