package com.chat.types.discussion;

/**
 * Created by tyler on 8/17/16.
 */
public enum DiscussionRole {
    CREATOR(1), USER(2), BLOCKED(3);

    int val;
    DiscussionRole(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }
}