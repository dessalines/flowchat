package com.chat.types;

import org.eclipse.jetty.websocket.api.Session;

/**
 * This helps determine what information gets broadcast to who.
 * Users only get updates for the discussion they're in, and for the
 * top comment parent(maybe null) that they are currently viewing
 * Created by tyler on 6/11/16.
 */
public class SessionScope {
    private final Session session;
    private final UserObj userObj;
    private Long discussionId;
    private Long topParentId;

    private SessionScope(Session session, UserObj userObj, Long discussionId, Long topParentId) {
        this.session = session;
        this.userObj = userObj;
        this.discussionId = discussionId;
        this.topParentId = topParentId;
    }


    public Long getDiscussionId() {
        return discussionId;
    }

    public Session getSession() {
        return session;
    }

    public UserObj getUserObj() {
        return userObj;
    }

    public Long getTopParentId() {
        return topParentId;
    }

    public void setDiscussionId(Long discussionId) {
        this.discussionId = discussionId;
    }

    public void setTopParentId(Long topParentId) {
        this.topParentId = topParentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SessionScope that = (SessionScope) o;

        if (!session.equals(that.session)) return false;
        if (!userObj.equals(that.userObj)) return false;
        if (!discussionId.equals(that.discussionId)) return false;
        return topParentId != null ? topParentId.equals(that.topParentId) : that.topParentId == null;

    }

    @Override
    public int hashCode() {
        int result = session.hashCode();
        result = 31 * result + userObj.hashCode();
        result = 31 * result + discussionId.hashCode();
        result = 31 * result + (topParentId != null ? topParentId.hashCode() : 0);
        return result;
    }
}
