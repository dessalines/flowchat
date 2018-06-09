package com.chat.types;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.chat.tools.Tools;
import com.chat.types.comment.Comment;
import com.chat.types.user.User;

import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * This helps determine what information gets broadcast to who.
 * Users only get updates for the discussion they're in, and for the
 * top comment parent(maybe null) that they are currently viewing
 * Created by tyler on 6/11/16.
 */
public class SessionScope {

    public static Logger log = (Logger) LoggerFactory.getLogger(SessionScope.class);

    private final Session session;
    private final User userObj;
    private Long discussionId;
    private Long topParentId;
    private String sortType;

    public SessionScope(Session session, User userObj, Long discussionId, Long topParentId, String sortType) {
        this.session = session;
        this.userObj = userObj;
        this.discussionId = discussionId;
        this.topParentId = topParentId;
        this.sortType = sortType;
    }

    public static Set<User> getUserObjects(Set<SessionScope> scopes) {
        return scopes.stream().map(SessionScope::getUserObj).collect(Collectors.toSet());
    }

    public static Set<Session> getSessions(Set<SessionScope> scopes) {
        return scopes.stream().map(SessionScope::getSession).collect(Collectors.toSet());
    }

    public static SessionScope findBySession(Set<SessionScope> scopes, Session session) {
        return scopes.stream().filter(s -> s.getSession().equals(session))
                .collect(Collectors.toSet()).iterator().next();
    }

    public static Set<SessionScope> constructFilteredMessageScopesFromSessionRequest(
            Set<SessionScope> scopes, Session session, List<Long> breadcrumbs) {


        Set<SessionScope> filteredScopes;
        Long discussionId = getDiscussionIdFromSession(session);

        log.debug(Arrays.toString(breadcrumbs.toArray()));
        log.debug(scopes.toString());

        filteredScopes = scopes.stream()
                .filter(s -> s.getDiscussionId().equals(discussionId) &&
                        // Send it to all top levels(null top), or those who have the parent in their crumbs
                        (s.getTopParentId() == null || breadcrumbs.contains(s.getTopParentId())))
                .collect(Collectors.toSet());


        return filteredScopes;

    }

    public static Set<SessionScope> constructFilteredUserScopesFromSessionRequest(
            Set<SessionScope> scopes, Session session) {

            Set<SessionScope> filteredScopes;
            Long discussionId = getDiscussionIdFromSession(session);

            filteredScopes = scopes.stream()
                    .filter(s -> s.getDiscussionId().equals(discussionId))
                    .collect(Collectors.toSet());

            return filteredScopes;


    }

    public static Long getTopParentIdFromSession(Session session) {
        Long topParentId = null;

        String maybeUndefined = session.getUpgradeRequest().getParameterMap().get("topParentId").iterator().next();

        if (!maybeUndefined.equals("NaN")) {
            topParentId = Long.valueOf(maybeUndefined);
        }

        return topParentId;
    }

    public static Long getDiscussionIdFromSession(Session session) {
        return Long.valueOf(session.getUpgradeRequest().getParameterMap().get("discussionId").iterator().next());
    }

    public static String getSortTypeFromSession(Session session) {
        return session.getUpgradeRequest().getParameterMap().get("sortType").iterator().next();
    }

    public static User getUserFromSession(Session session) {
        Map<String, String> cookieMap = Tools.cookieListToMap(session.getUpgradeRequest().getCookies());
        String jwt = cookieMap.get("jwt");
    
        return User.create(jwt);
      }

    public Long getDiscussionId() {
        return discussionId;
    }

    public Session getSession() {
        return session;
    }

    public User getUserObj() {
        return userObj;
    }

    public Long getTopParentId() {
        return topParentId;
    }

    public Comparator<Comment> getCommentComparator() {
        if (this.sortType.equals("new")) {
            return new Comment.CommentObjComparatorNew();
        } else if (this.sortType.equals("top")) {
            return new Comment.CommentObjComparatorTop();
        } else {
            return new Comment.CommentObjComparatorHot();
        }
        
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
                ", userObj=" + userObj +
                ", discussionId=" + discussionId +
                ", topParentId=" + topParentId +
                ", sortTYpe=" + sortType +
                '}';
    }
}
