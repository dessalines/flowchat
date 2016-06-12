package com.chat.types;

import ch.qos.logback.classic.Logger;
import com.chat.tools.Tools;
import com.chat.webservice.ThreadedChatWebSocket;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This helps determine what information gets broadcast to who.
 * Users only get updates for the discussion they're in, and for the
 * top comment parent(maybe null) that they are currently viewing
 * Created by tyler on 6/11/16.
 */
public class SessionScope {

    public static Logger log = (Logger) LoggerFactory.getLogger(SessionScope.class);


    private final Session session;
    private final UserObj userObj;
    private Long discussionId;
    private Long topParentId;

    public SessionScope(Session session, UserObj userObj, Long discussionId, Long topParentId) {
        this.session = session;
        this.userObj = userObj;
        this.discussionId = discussionId;
        this.topParentId = topParentId;
    }

    public static Set<SessionScope> getFilteredSessionScopes(
            Set<SessionScope> scopes,
            Long discussionId,
            Long topParentId) {

        Set<SessionScope> filteredScopes;

        if (topParentId != null) {
            filteredScopes = scopes.stream()
                    .filter(s -> s.getDiscussionId().equals(discussionId)
                            && s.getTopParentId().equals(topParentId))
                    .collect(Collectors.toSet());
        } else {
            filteredScopes = scopes.stream()
                    .filter(s -> s.getDiscussionId().equals(discussionId))
                    .collect(Collectors.toSet());
        }

        return filteredScopes;

    }

    public static Set<UserObj> getUserObjects(Set<SessionScope> scopes) {
        return scopes.stream().map(SessionScope::getUserObj).collect(Collectors.toSet());
    }

    public static Set<Session> getSessions(Set<SessionScope> scopes) {
        return scopes.stream().map(SessionScope::getSession).collect(Collectors.toSet());
    }

    public static SessionScope findBySession(Set<SessionScope> scopes, Session session) {
        return scopes.stream().filter(s -> s.getSession().equals(session))
                .collect(Collectors.toSet()).iterator().next();
    }

    public static Set<SessionScope> constructFilteredScopesFromSessionRequest(
            Set<SessionScope> scopes, Session session) {


        // Send the updated users to everyone in the discussion and parent tree(session scope)
        Set<SessionScope> filteredScopes = SessionScope.getFilteredSessionScopes(
                scopes, getDiscussionIdFromSession(session),
                getTopParentIdFromSession(session));

        return filteredScopes;

    }

    public static Long getTopParentIdFromSession(Session session) {
        Long topParentId = null;

        String maybeUndefined = session.getUpgradeRequest().getParameterMap().get("topParentId").iterator().next();

        if (!maybeUndefined.equals("null")) {
            topParentId = Long.valueOf(maybeUndefined);
        }

        return topParentId;
    }

    public static Long getDiscussionIdFromSession(Session session) {
        return Long.valueOf(session.getUpgradeRequest().getParameterMap().get("discussionId").iterator().next());
    }

    public static String getAuthFromSession(Session session) {
        Map<String, String> cookieMap = Tools.cookieListToMap(session.getUpgradeRequest().getCookies());

        return cookieMap.get("auth");
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

    @Override
    public String toString() {
        return "SessionScope{" +
                "session=" + session +
                ", userObj=" + userObj +
                ", discussionId=" + discussionId +
                ", topParentId=" + topParentId +
                '}';
    }
}
