package com.chat.webservice;

import ch.qos.logback.classic.Logger;
import com.chat.db.Actions;
import com.chat.tools.Tools;
import com.chat.types.SessionScope;
import com.chat.types.comment.Comment;
import com.chat.types.comment.Comments;
import com.chat.types.discussion.Discussion;
import com.chat.types.user.User;
import com.chat.types.user.Users;
import com.chat.types.websocket.input.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.javalite.activejdbc.LazyList;
import org.javalite.activejdbc.Model;
import org.slf4j.LoggerFactory;

import java.sql.Array;
import java.util.*;

import static com.chat.db.Tables.*;

/**
 * Created by tyler on 6/5/16.
 */

@WebSocket
public class ThreadedChatWebSocket {

  private static Long topLimit = 20L;
  private static Long maxDepth = 20L;

  public static Logger log = (Logger) LoggerFactory.getLogger(ThreadedChatWebSocket.class);

  static Set<SessionScope> sessionScopes = new HashSet<>();

  private static final Integer PING_DELAY = 10000;

  enum MessageType {
    Comments, Users, Edit, Reply, TopReply, Vote, Delete, NextPage, Sticky, SaveFavoriteDiscussion, Ping, Pong;
  }

  public ThreadedChatWebSocket() {
  }

  @OnWebSocketConnect
  public void onConnect(Session session) {

    try {
      Tools.dbInit();

      // Get or create the session scope
      SessionScope ss = setupSessionScope(session);

      sendRecurringPings(session);

      // Send them their user info
      // TODO
      // session.getRemote().sendString(ss.getUserObj().json("user"));

      LazyList<Model> comments = fetchComments(ss);

      // send the comments
      sendMessage(session,
          messageWrapper(MessageType.Comments, Comments
              .create(comments, fetchVotesMap(ss.getUserObj().getId()), topLimit, maxDepth, ss.getCommentComparator())
              .json()));

      // send the updated users to everyone in the right scope(just discussion)
      Set<SessionScope> filteredScopes = SessionScope.constructFilteredUserScopesFromSessionRequest(sessionScopes,
          session);
      broadcastMessage(filteredScopes,
          messageWrapper(MessageType.Users, Users.create(SessionScope.getUserObjects(filteredScopes)).json()));

      log.debug("session scope " + ss + " joined");

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      Tools.dbClose();
    }

  }

  @OnWebSocketClose
  public void onClose(Session session, int statusCode, String reason) {

    SessionScope ss = SessionScope.findBySession(sessionScopes, session);
    sessionScopes.remove(ss);

    log.debug("session scope " + ss + " left, " + statusCode + " " + reason);

    // Send the updated users to everyone in the right scope
    Set<SessionScope> filteredScopes = SessionScope.constructFilteredUserScopesFromSessionRequest(sessionScopes,
        session);

    broadcastMessage(filteredScopes,
        messageWrapper(MessageType.Users, Users.create(SessionScope.getUserObjects(filteredScopes)).json()));

  }

  @OnWebSocketMessage
  public void onMessage(Session session, String dataStr) {
    try {
      Tools.dbInit();
      JsonNode node = null;
      node = Tools.JACKSON.readTree(dataStr);

      JsonNode data = node.get("data");

      switch (getMessageType(node)) {
      case Reply:
        messageReply(session, data);
        break;
      case Edit:
        messageEdit(session, data);
        break;
      case Sticky:
        messageSticky(session, data);
        break;
      case TopReply:
        messageTopReply(session, data);
        break;
      case Delete:
        messageDelete(session, data);
        break;
      case Vote:
        saveCommentVote(session, data);
        break;
      case NextPage:
        messageNextPage(session, data);
        break;
      case Pong:
        pongReceived(session, data);
        break;
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      Tools.dbClose();
    }

  }

  public MessageType getMessageType(JsonNode node) {
    return MessageType.values()[node.get("message_type").asInt()];
  }

  public void messageNextPage(Session session, JsonNode data) {
    SessionScope ss = SessionScope.findBySession(sessionScopes, session);

    // Get the object
    NextPageData nextPageData = new NextPageData(data.get("topLimit").asLong(), data.get("maxDepth").asLong());

    // Refetch the comments based on the new limit
    LazyList<Model> comments = fetchComments(ss);

    // send the comments from up to the new limit to them
    sendMessage(session,
        messageWrapper(MessageType.Comments, Comments.create(comments, fetchVotesMap(ss.getUserObj().getId()),
            nextPageData.getTopLimit(), nextPageData.getMaxDepth(), ss.getCommentComparator()).json()));

  }

  public void messageReply(Session session, JsonNode data) {

    SessionScope ss = SessionScope.findBySession(sessionScopes, session);

    // Get the object
    ReplyData replyData = new ReplyData(data.get("parentId").asLong(), data.get("reply").asText());

    // Collect only works on refetch
    LazyList<Model> comments = fetchComments(ss);

    log.debug(ss.toString());

    // Necessary for comment tree
    Array arr = (Array) comments.collect("breadcrumbs", "id", replyData.getParentId()).get(0);
    List<Long> parentBreadCrumbs = Tools.convertArrayToList(arr);

    com.chat.db.Tables.Comment newComment = Actions.createComment(ss.getUserObj().getId(), ss.getDiscussionId(),
        parentBreadCrumbs, replyData.getReply());

    // Fetch the comment threaded view
    CommentThreadedView ctv = CommentThreadedView.findFirst("id = ?", newComment.getLongId());

    // Convert to a proper commentObj
    Comment co = Comment.create(ctv, null);

    Set<SessionScope> filteredScopes = SessionScope.constructFilteredMessageScopesFromSessionRequest(sessionScopes,
        session, co.getBreadcrumbs());

    broadcastMessage(filteredScopes, messageWrapper(MessageType.Reply, co.json()));

    // TODO find a way to do this without having to query every time?
    com.chat.types.discussion.Discussion do_ = Actions.saveFavoriteDiscussion(ss.getUserObj().getId(),
        ss.getDiscussionId());
    if (do_ != null)
      sendMessage(session, messageWrapper(MessageType.SaveFavoriteDiscussion, do_.json()));
  }

  public void messageEdit(Session session, JsonNode data) {

    SessionScope ss = SessionScope.findBySession(sessionScopes, session);

    EditData editData = new EditData(data.get("id").asLong(), data.get("edit").asText());

    com.chat.db.Tables.Comment c = Actions.editComment(ss.getUserObj().getId(), editData.getId(), editData.getEdit());

    CommentThreadedView ctv = CommentThreadedView.findFirst("id = ?", c.getLongId());

    // Convert to a proper commentObj, but with nothing embedded
    Comment co = Comment.create(ctv, null);

    Set<SessionScope> filteredScopes = SessionScope.constructFilteredMessageScopesFromSessionRequest(sessionScopes,
        session, co.getBreadcrumbs());

    broadcastMessage(filteredScopes, messageWrapper(MessageType.Edit, co.json()));

  }

  public void messageSticky(Session session, JsonNode data) {

    StickyData stickyData = new StickyData(data.get("id").asLong(), data.get("sticky").asBoolean());

    com.chat.db.Tables.Comment c = Actions.stickyComment(stickyData.getId(), stickyData.getSticky());

    CommentThreadedView ctv = CommentThreadedView.findFirst("id = ?", c.getLongId());

    // Convert to a proper commentObj, but with nothing embedded
    Comment co = Comment.create(ctv, null);

    Set<SessionScope> filteredScopes = SessionScope.constructFilteredMessageScopesFromSessionRequest(sessionScopes,
        session, co.getBreadcrumbs());

    // Send an edit with the sticky info
    broadcastMessage(filteredScopes, messageWrapper(MessageType.Edit, co.json()));
  }

  public void messageDelete(Session session, JsonNode data) {

    SessionScope ss = SessionScope.findBySession(sessionScopes, session);

    DeleteData deleteData = new DeleteData(data.get("deleteId").asLong());

    com.chat.db.Tables.Comment c = Actions.deleteComment(ss.getUserObj().getId(), deleteData.getDeleteId());

    CommentThreadedView ctv = CommentThreadedView.findFirst("id = ?", c.getLongId());

    // Convert to a proper commentObj, but with nothing embedded
    Comment co = Comment.create(ctv, null);

    Set<SessionScope> filteredScopes = SessionScope.constructFilteredMessageScopesFromSessionRequest(sessionScopes,
        session, co.getBreadcrumbs());

    broadcastMessage(filteredScopes, messageWrapper(MessageType.Delete, co.json()));

  }

  public void messageTopReply(Session session, JsonNode data) {

    SessionScope ss = SessionScope.findBySession(sessionScopes, session);

    // Get the object
    TopReplyData topReplyData = new TopReplyData(data.get("topReply").asText());

    com.chat.db.Tables.Comment newComment = Actions.createComment(ss.getUserObj().getId(), ss.getDiscussionId(), null,
        topReplyData.getTopReply());

    // Fetch the comment threaded view
    CommentThreadedView ctv = CommentThreadedView.findFirst("id = ?", newComment.getLongId());

    // Convert to a proper commentObj
    Comment co = Comment.create(ctv, null);

    Set<SessionScope> filteredScopes = SessionScope.constructFilteredMessageScopesFromSessionRequest(sessionScopes,
        session, co.getBreadcrumbs());

    broadcastMessage(filteredScopes, messageWrapper(MessageType.TopReply, co.json()));

    // TODO find a way to do this without having to query every time?
    Discussion do_ = Actions.saveFavoriteDiscussion(ss.getUserObj().getId(), ss.getDiscussionId());
    if (do_ != null)
      sendMessage(session, messageWrapper(MessageType.SaveFavoriteDiscussion, do_.json()));

  }

  public static void saveCommentVote(Session session, JsonNode data) {

    SessionScope ss = SessionScope.findBySession(sessionScopes, session);

    // Get the object
    CommentRankData commentRankData = new CommentRankData(data.get("rank").asInt(), data.get("commentId").asLong());

    Long userId = ss.getUserObj().getId();
    log.debug(userId.toString());
    Long commentId = commentRankData.getCommentId();
    Integer rank = commentRankData.getRank();

    String message = Actions.saveCommentVote(userId, commentId, rank);

    // Getting the comment for the breadcrumbs for the scope
    CommentThreadedView ctv = CommentThreadedView.findFirst("id = ?", commentId);

    // Convert to a proper commentObj, but with nothing embedded
    Comment co = Comment.create(ctv, null);

    Set<SessionScope> filteredScopes = SessionScope.constructFilteredMessageScopesFromSessionRequest(sessionScopes,
        session, co.getBreadcrumbs());

    // This sends an edit, which contains the average rank
    broadcastMessage(filteredScopes, messageWrapper(MessageType.Edit, co.json()));

  }

  // Sends a message from one user to all users
  // TODO need to get subsets of sessions based on discussion_id, and parent_id
  // Maybe Map<discussion_id, List<sessions>

  public static void broadcastMessage(Set<SessionScope> filteredScopes, String json) {
    SessionScope.getSessions(filteredScopes).stream().filter(Session::isOpen).forEach(session -> {
      try {
        session.getRemote().sendString(json);
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }

  public static void sendMessage(Session session, String json) {
    try {
      session.getRemote().sendString(json);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private SessionScope setupSessionScope(Session session) {

    User userObj = SessionScope.getUserFromSession(session);
    Long discussionId = SessionScope.getDiscussionIdFromSession(session);
    Long topParentId = SessionScope.getTopParentIdFromSession(session);
    String sortType = SessionScope.getSortTypeFromSession(session);

    log.debug(userObj.json());

    SessionScope ss = new SessionScope(session, userObj, discussionId, topParentId, sortType);
    sessionScopes.add(ss);

    return ss;

  }

  private static LazyList<Model> fetchComments(SessionScope scope) {
    if (scope.getTopParentId() != null) {
      return CommentBreadcrumbsView.where("discussion_id = ? and parent_id = ?", scope.getDiscussionId(),
          scope.getTopParentId());
    } else {
      return CommentThreadedView.where("discussion_id = ?", scope.getDiscussionId());
    }
  }

  // These create maps from a user's comment id, to their rank/vote
  private static Map<Long, Integer> fetchVotesMap(Long userId) {
    List<CommentRank> ranks = CommentRank.where("user_id = ?", userId);

    return convertCommentRanksToVoteMap(ranks);
  }

  private static Map<Long, Integer> fetchVotesMap(Long userId, Long commentId) {
    List<CommentRank> ranks = CommentRank.where("comment_id = ? and user_id = ?", commentId, userId);

    return convertCommentRanksToVoteMap(ranks);

  }

  private static Map<Long, Integer> convertCommentRanksToVoteMap(List<CommentRank> ranks) {
    Map<Long, Integer> map = new HashMap<>();

    for (CommentRank rank : ranks) {
      map.put(rank.getLong("comment_id"), rank.getInteger("rank"));
    }
    return map;
  }

  private void sendRecurringPings(Session session) {
    final Timer timer = new Timer();
    final TimerTask tt = new TimerTask() {
      @Override
      public void run() {
        if (session.isOpen()) {
          sendMessage(session, messageWrapper(MessageType.Ping, "{\"ping\":\"ping\"}"));
        } else {
          timer.cancel();
          timer.purge();
        }
      }
    };

    timer.scheduleAtFixedRate(tt, PING_DELAY, PING_DELAY);
  }

  private void pongReceived(Session session, JsonNode pongStr) {
    log.debug("Pong received from " + session.getRemoteAddress());
  }

  private static String messageWrapper(MessageType type, String data) {
    return "{\"message_type\":" + type.ordinal() + ",\"data\":" + data + "}";
  }

}
