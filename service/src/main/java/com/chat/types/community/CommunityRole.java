package com.chat.types.community;

/**
 * Created by tyler on 8/18/16.
 */
public enum CommunityRole {
    CREATOR(1), MODERATOR(2), USER(3), BLOCKED(4);

    int val;
    CommunityRole(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }
}
