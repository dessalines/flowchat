package com.chat.webservice;

import static spark.Spark.after;
import static spark.Spark.before;
import static spark.Spark.delete;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;

import java.util.Map;
import java.util.Set;

import com.chat.DataSources;
import com.chat.db.Actions;
import com.chat.db.Tables;
import com.chat.db.Tables.UserSetting;
import com.chat.tools.Tools;
import com.chat.types.comment.Comments;
import com.chat.types.community.Communities;
import com.chat.types.community.Community;
import com.chat.types.community.CommunityRole;
import com.chat.types.discussion.Discussion;
import com.chat.types.discussion.Discussions;
import com.chat.types.tag.Tag;
import com.chat.types.tag.Tags;
import com.chat.types.user.User;
import com.chat.types.user.UserSettings;
import com.chat.types.user.Users;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;

import org.eclipse.jetty.http.HttpStatus;
import org.javalite.activejdbc.LazyList;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * Created by tyler on 7/29/16.
 */
public class Endpoints {

  public static Logger log = (Logger) LoggerFactory.getLogger(Endpoints.class);

  public static void status() {

    get("/version", (req, res) -> {
      return "{\"version\":\"" + DataSources.PROPERTIES.getProperty("version") + "\"}";
    });

    before((req, res) -> {
      res.header("Access-Control-Allow-Origin", "*");
      res.header("Access-Control-Allow-Credentials", "true");
      res.header("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, token, X-Requested-With");
      res.header("Access-Control-Allow-Methods", "POST, GET, PUT, DELETE, OPTIONS");
      Tools.dbInit();
    });

    after((req, res) -> {
      res.header("Content-Encoding", "gzip");
      Tools.dbClose();
    });

    exception(Exception.class, (e, req, res) -> {
      e.printStackTrace();
      log.error(req.uri());
      Tools.dbClose();
      res.status(HttpStatus.BAD_REQUEST_400);
      res.body(e.getMessage());
    });

    // exception(NullPointerException.class, (e, req, res) -> {
    // e.printStackTrace();
    // log.error(req.uri());
    // Tools.dbClose();
    // });

    // exception(NoSuchElementException.class, (e, req, res) -> {
    // e.printStackTrace();
    // log.error(req.uri());
    // Tools.dbClose();
    // });

  }

  public static void user() {

    post("/user", (req, res) -> {

      Map<String, String> vars = Tools.createMapFromReqBody(req.body());

      String name = vars.get("name");

      User user = (name != null) ? Actions.createNewSimpleUser(name) : Actions.createNewAnonymousUser();

      return user.getJwt();

    });

    post("/login", (req, res) -> {

      Map<String, String> vars = Tools.createMapFromReqBody(req.body());

      String userOrEmail = vars.get("usernameOrEmail");
      String password = vars.get("password");

      User userObj = Actions.login(userOrEmail, password);

      return userObj.getJwt();

    });

    post("/signup", (req, res) -> {

      Long userId = (req.headers("token") != null) ? Tools.getUserFromJWTHeader(req).getId() : null;

      Map<String, String> vars = Tools.createMapFromReqBody(req.body());

      String userName = vars.get("username");
      String password = vars.get("password");
      String verifyPassword = vars.get("verifyPassword");
      String email = vars.get("email");

      User userObj = Actions.signup(userId, userName, password, verifyPassword, email);

      return userObj.getJwt();

    });

    get("/user_search/:query", (req, res) -> {

      String query = req.params(":query");

      String queryStr = Tools.constructQueryString(query, "name");

      LazyList<Tables.User> userRows = Tables.User.find(queryStr.toString()).limit(5);

      Users users = Users.create(userRows);

      return users.json();

    });

    get("/user_log/:id", (req, res) -> {

      Long userId = Long.valueOf(req.params(":id"));

      LazyList<Tables.UserAuditView> auditRows = Tables.UserAuditView.find("user_id = ?", userId);

      String json = auditRows.toJson(false, "action", "action_tstamp", "comment_text", "community_id", "community_name",
          "discussion_id", "discussion_title", "id", "modified_by_user_id", "modified_by_user_name", "table_name",
          "user_id", "user_name", "role_id");

      // Converting with jackson corrects the double quote issues
      Tools.JACKSON.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);

      JsonNode j = Tools.JACKSON.readTree(json);

      // json = json.replaceAll("\\\\", "\n").replaceAll("\n", "\\n");

      return j.toString();

    });

    get("/user_setting", (req, res) -> {
      User userObj = Tools.getUserFromJWTHeader(req);

      UserSetting us = UserSetting.findFirst("user_id = ?", userObj.getId());

      UserSettings uv = UserSettings.create(us);

      return uv.json();
    });

    put("/user_setting", (req, res) -> {
      User userObj = Tools.getUserFromJWTHeader(req);
      Map<String, String> vars = Tools.createMapFromReqBody(req.body());
      Actions.saveUserSettings(userObj.getId(), vars.get("defaultViewTypeRadioValue"),
          vars.get("defaultSortTypeRadioValue"), vars.get("defaultCommentSortTypeRadioValue"),
          Boolean.valueOf(vars.get("readOnboardAlert")), Integer.valueOf(vars.get("theme")));

      res.status(HttpStatus.OK_200);

      return "{}";
    });

  }

  public static void tag() {

    get("/tag/:id", (req, res) -> {

      Long id = Long.valueOf(req.params(":id"));

      Tables.Tag t = Tables.Tag.findFirst("id = ?", id);

      Tag to = Tag.create(t);

      return to.json();

    });

    get("/tag_search/:query", (req, res) -> {

      String query = req.params(":query");

      String queryStr = Tools.constructQueryString(query, "name");

      LazyList<Tables.Tag> tagRows = Tables.Tag.find(queryStr.toString()).limit(5);

      Tags tags = Tags.create(tagRows);

      return tags.json();

    });

    post("/tag", (req, res) -> {

      String name = Tools.createMapFromReqBody(req.body()).get("name");

      Tag to = Actions.createTag(name);

      res.status(HttpStatus.CREATED_201);

      return to.json();

    });

    get("/tags/:limit/:page/:orderBy", (req, res) -> {

      Integer limit = (req.params(":limit") != null) ? Integer.valueOf(req.params(":limit")) : 10;
      Integer page = (req.params(":page") != null) ? Integer.valueOf(req.params(":page")) : 1;
      String orderBy = (req.params(":orderBy") != null) ? req.params(":orderBy") : "custom";
      Integer offset = (page - 1) * limit;

      orderBy = Tools.constructOrderByPopularTagsCustom(orderBy);

      LazyList<Tables.TagsView> popularTags = Tables.TagsView.findAll().orderBy(orderBy).limit(limit).offset(offset);

      return popularTags.toJson(false);

    });

  }

  public static void discussion() {

    get("/discussion/:id", (req, res) -> {

      Long id = Long.valueOf(req.params(":id"));

      User userObj = Tools.getUserFromJWTHeader(req);

      Tables.DiscussionFullView dfv = Tables.DiscussionFullView.findFirst("id = ?", id);

      // Get your vote for the discussion:
      Tables.DiscussionRank dr = Tables.DiscussionRank.findFirst("discussion_id = ? and user_id = ?", id,
          userObj.getId());

      Integer vote = (dr != null) ? dr.getInteger("rank") : null;

      // Get the tags for those discussions:
      LazyList<Tables.DiscussionTagView> tags = Tables.DiscussionTagView.where("discussion_id = ?", id);

      // Get the users for those discussions
      LazyList<Tables.DiscussionUserView> users = Tables.DiscussionUserView.where("discussion_id = ?", id);

      Tables.CommunityNoTextView community = Tables.CommunityNoTextView.findFirst("id = ?",
          dfv.getLong("community_id"));

      // Get the users for that community
      LazyList<Tables.CommunityUserView> communityUsers = Tables.CommunityUserView.where("community_id = ?",
          community.getLong("id"));

      Discussion df = Discussion.create(dfv, community, tags, users, communityUsers, vote);

      // check to make sure user is entitled to view it
      df.checkPrivate(userObj);

      // Check to make sure user isn't blocked
      df.checkBlocked(userObj);

      // check to make sure user is entitled to view the community
      df.getCommunity().checkPrivate(userObj);

      // Check to make sure user is isn't blocked from the community
      df.getCommunity().checkBlocked(userObj);

      return df.json();

    });

    // Get the user id
    // A test query
    // select title, created, number_of_votes, avg_rank, ranking(created, 86400,
    // number_of_votes, 0.1, avg_rank, 0.01) from discussion_notext_view order by
    // ranking(created, 86400, number_of_votes, 0.1, avg_rank, 0.01) desc limit 200;
    get("/discussions/:tagId/:communityId/:limit/:page/:orderBy", (req, res) -> {

      Long tagId = (!req.params(":tagId").equals("all")) ? Long.valueOf(req.params(":tagId")) : null;
      Integer limit = (req.params(":limit") != null) ? Integer.valueOf(req.params(":limit")) : 10;
      Integer page = (req.params(":page") != null) ? Integer.valueOf(req.params(":page")) : 1;
      Integer offset = (page - 1) * limit;

      User userObj = Tools.getUserFromJWTHeader(req);

      Set<Long> communityIds = Tools.fetchCommunitiesFromParams(req.params(":communityId"), userObj);

      String orderBy = (req.params(":orderBy") != null) ? req.params(":orderBy")
          : "time-" + ConstantsService.INSTANCE.getRankingConstants().getCreatedWeight().intValue();

      Boolean singleCommunity = (req.params(":communityId") != null && !req.params(":communityId").equals("all")
          && !req.params(":communityId").equals("favorites"));

      orderBy = Tools.constructOrderByCustom(orderBy, singleCommunity);

      LazyList<Tables.DiscussionNoTextView> dntvs;
      // TODO refactor this to a communitiesQueryBuilder, same with discussion(don't
      // use parameterized anymore)
      if (tagId != null) {
        if (communityIds != null) {
          dntvs = Tables.DiscussionNoTextView
              .find("tag_ids @> ARRAY[?]::bigint[] " + "and community_id in " + Tools.convertListToInQuery(communityIds)
                  + " " + "and private is false and deleted is false and title != ?", tagId, "A new discussion")
              .orderBy(orderBy).limit(limit).offset(offset);
        } else {
          dntvs = Tables.DiscussionNoTextView
              .find("tag_ids @> ARRAY[?]::bigint[] " + "and private is false and deleted is false and title != ?",
                  tagId, "A new discussion")
              .orderBy(orderBy).limit(limit).offset(offset);
        }

      } else {
        if (communityIds != null) {
          dntvs = Tables.DiscussionNoTextView
              .find("community_id in " + Tools.convertListToInQuery(communityIds) + " "
                  + "and private is false and deleted is false and title != ?", "A new discussion")
              .orderBy(orderBy).limit(limit).offset(offset);
        }
        // Don't show nsfw in all
        else {
          dntvs = Tables.DiscussionNoTextView
              .find("private is false and deleted is false and nsfw is false and title != ?", "A new discussion")
              .orderBy(orderBy).limit(limit).offset(offset);
        }
      }

      log.debug(dntvs.toSql(true));

      Discussions discussions = null;
      if (!dntvs.isEmpty()) {

        // Get the list of discussions
        Set<Long> ids = dntvs.collectDistinct("id");

        // Get a list of the communities
        communityIds = dntvs.collectDistinct("community_id");

        // Get your votes for those discussions:
        LazyList<Tables.DiscussionRank> votes = Tables.DiscussionRank
            .where("discussion_id in " + Tools.convertListToInQuery(ids) + " and user_id = ?", userObj.getId());

        // Get the tags for those discussions:
        LazyList<Tables.DiscussionTagView> tags = Tables.DiscussionTagView
            .where("discussion_id in " + Tools.convertListToInQuery(ids));

        // Get the users for those discussions
        LazyList<Tables.DiscussionUserView> users = Tables.DiscussionUserView
            .where("discussion_id in " + Tools.convertListToInQuery(ids));

        // Get the communities for those discussions
        LazyList<Tables.CommunityNoTextView> communities = Tables.CommunityNoTextView
            .where("id in " + Tools.convertListToInQuery(communityIds));

        // Build discussion objects
        discussions = Discussions.create(dntvs, communities, tags, users, votes, 999L);

      } else {
        discussions = Discussions.create(dntvs, null, null, null, null, 999L);
      }

      return discussions.json();

    });

    get("/discussion_search/:query", (req, res) -> {

      String query = req.params(":query");

      String queryStr = Tools.constructQueryString(query, "title");

      LazyList<Tables.DiscussionNoTextView> discussionsRows = Tables.DiscussionNoTextView
          .find("deleted is false and " + queryStr.toString()).limit(5);

      Discussions discussions = Discussions.create(discussionsRows, null, null, null, null,
          Long.valueOf(discussionsRows.size()));

      return discussions.json();

    });

    post("/discussion_rank/:id/:rank", (req, res) -> {

      User userObj = Tools.getUserFromJWTHeader(req);

      Long discussionId = Long.valueOf(req.params(":id"));

      Integer rank = (!req.params(":rank").equals("null")) ? Integer.valueOf(req.params(":rank")) : null;

      Actions.saveDiscussionVote(userObj.getId(), discussionId, rank);

      res.status(HttpStatus.OK_200);

      return "{}";

    });

    post("/discussion_blank", (req, res) -> {

      User userObj = Tools.getUserFromJWTHeader(req);

      Discussion do_ = Actions.createDiscussionEmpty(userObj.getId());

      res.status(HttpStatus.CREATED_201);

      return do_.json();

    });

    post("/discussion", (req, res) -> {

      User userObj = Tools.getUserFromJWTHeader(req);

      Discussion doIn = Discussion.fromJson(req.body());

      Discussion do_ = Actions.createDiscussion(userObj.getId(), doIn);

      res.status(HttpStatus.CREATED_201);

      return do_.json();

    });

    put("/discussion", (req, res) -> {

      User userObj = Tools.getUserFromJWTHeader(req);

      Discussion doIn = Discussion.fromJson(req.body());

      Discussion do_ = Actions.saveDiscussion(userObj.getId(), doIn);

      res.status(HttpStatus.OK_200);

      return do_.json();

    });

    get("/favorite_discussions", (req, res) -> {

      User userObj = Tools.getUserFromJWTHeader(req);

      LazyList<Tables.FavoriteDiscussionUserView> favs = Tables.FavoriteDiscussionUserView
          .where("user_id = ? and deleted = ?", userObj.getId(), false);

      Set<Long> favDiscussionIds = favs.collectDistinct("discussion_id");

      String json = "";
      if (favDiscussionIds.size() > 0) {
        LazyList<Tables.DiscussionNoTextView> dntv = Tables.DiscussionNoTextView
            .where("id in " + Tools.convertListToInQuery(favDiscussionIds));

        Discussions d = Discussions.create(dntv, null, null, null, null, Long.valueOf(dntv.size()));

        json = d.json();
      } else {
        json = "{\"Discussions\": []}";
      }

      return json;

    });

    delete("/favorite_discussion/:id", (req, res) -> {

      User userObj = Tools.getUserFromJWTHeader(req);

      Long discussionId = Long.valueOf(req.params(":id"));

      Actions.deleteFavoriteDiscussion(userObj.getId(), discussionId);

      res.status(HttpStatus.OK_200);

      return "{}";

    });
  }

  public static void reply() {

    get("/unread_replies", (req, res) -> {

      User userObj = Tools.getUserFromJWTHeader(req);

      // Fetch your unread replies
      LazyList<Tables.CommentBreadcrumbsView> cbv = Tables.CommentBreadcrumbsView
          .where("parent_user_id = ? and user_id != ? and read = ?", userObj.getId(), userObj.getId(), false);

      Comments comments = Comments.replies(cbv);

      return comments.json();

    });

    post("/mark_reply_as_read/:id", (req, res) -> {

      User userObj = Tools.getUserFromJWTHeader(req);

      Long commentId = Long.valueOf(req.params(":id"));

      // Mark the reply as read
      Actions.markReplyAsRead(commentId);

      res.status(HttpStatus.OK_200);

      return "{}";

    });

    post("/mark_all_replies_as_read", (req, res) -> {

      User userObj = Tools.getUserFromJWTHeader(req);

      // Mark the reply as read
      Actions.markAllRepliesAsRead(userObj.getId());

      res.status(HttpStatus.OK_200);

      return "{}";

    });
  }

  public static void community() {

    get("/community/:id", (req, res) -> {

      Long id = Long.valueOf(req.params(":id"));

      User userObj = Tools.getUserFromJWTHeader(req);

      Tables.CommunityView cv = Tables.CommunityView.findFirst("id = ?", id);

      // Get your vote for the community:
      Tables.CommunityRank cr = Tables.CommunityRank.findFirst("community_id = ? and user_id = ?", id, userObj.getId());

      Integer vote = (cr != null) ? cr.getInteger("rank") : null;

      // Get the tags for that community:
      LazyList<Tables.CommunityTagView> tags = Tables.CommunityTagView.where("community_id = ?", id);

      // Get the users for that community
      LazyList<Tables.CommunityUserView> users = Tables.CommunityUserView.where("community_id = ?", id);

      Community co = Community.create(cv, tags, users, vote);

      // check to make sure user is entitled to view it
      co.checkPrivate(userObj);

      // Check to make sure user isn't blocked
      co.checkBlocked(userObj);

      return co.json();

    });

    // Get the user id
    get("/communities/:tagId/:limit/:page/:orderBy", (req, res) -> {

      Long tagId = (!req.params(":tagId").equals("all")) ? Long.valueOf(req.params(":tagId")) : null;
      Integer limit = (req.params(":limit") != null) ? Integer.valueOf(req.params(":limit")) : 10;
      Integer page = (req.params(":page") != null) ? Integer.valueOf(req.params(":page")) : 1;
      Integer offset = (page - 1) * limit;

      String orderBy = (req.params(":orderBy") != null) ? req.params(":orderBy")
          : "time-" + ConstantsService.INSTANCE.getRankingConstants().getCreatedWeight().intValue();

      orderBy = Tools.constructOrderByCustom(orderBy, false);
      User userObj = Tools.getUserFromJWTHeader(req);

      LazyList<Tables.CommunityNoTextView> cv;
      // TODO for now don't show where private is false
      if (tagId != null) {
        // fetch the tags
        cv = Tables.CommunityNoTextView
            .find("tag_ids @> ARRAY[?]::bigint[] " + "and private is false and deleted is false and name not like ?",
                tagId, "new_community%")
            .orderBy(orderBy).limit(limit).offset(offset);
      }
      // Don't fetch nsfw communities
      else {
        cv = Tables.CommunityNoTextView
            .find("private is false and deleted is false and nsfw is false and name not like ?", "new_community%")
            .orderBy(orderBy).limit(limit).offset(offset);
      }

      Communities communities;
      if (!cv.isEmpty()) {
        // Get the list of communities
        Set<Long> ids = cv.collectDistinct("id");

        // Get your votes for those communities:
        LazyList<Tables.CommunityRank> votes = Tables.CommunityRank
            .where("community_id in " + Tools.convertListToInQuery(ids) + " and user_id = ?", userObj.getId());

        // Get the tags for those communities:
        LazyList<Tables.CommunityTagView> tags = Tables.CommunityTagView
            .where("community_id in " + Tools.convertListToInQuery(ids));

        // Get the users for those communities
        LazyList<Tables.CommunityUserView> users = Tables.CommunityUserView
            .where("community_id in " + Tools.convertListToInQuery(ids));

        // Build community objects
        communities = Communities.create(cv, tags, users, votes, 999L);

      } else {
        communities = Communities.create(cv, null, null, null, 999L);
      }

      return communities.json();

    });

    get("/community_search/:query", (req, res) -> {

      String query = req.params(":query");

      String queryStr = Tools.constructQueryString(query, "name");

      LazyList<Tables.CommunityNoTextView> communityRows = Tables.CommunityNoTextView
          .find("deleted is false and " + queryStr.toString()).limit(5);

      Communities communities = Communities.create(communityRows, null, null, null, Long.valueOf(communityRows.size()));

      return communities.json();

    });

    post("/community_rank/:id/:rank", (req, res) -> {

      User userObj = Tools.getUserFromJWTHeader(req);

      Long id = Long.valueOf(req.params(":id"));
      Integer rank = Integer.valueOf(req.params(":rank"));

      Actions.saveCommunityVote(userObj.getId(), id, rank);

      res.status(HttpStatus.OK_200);

      return "{}";

    });

    post("/community", (req, res) -> {

      User userObj = Tools.getUserFromJWTHeader(req);

      Community co_ = Actions.createCommunity(userObj.getId());

      res.status(HttpStatus.CREATED_201);

      return co_.json();

    });

    put("/community", (req, res) -> {

      User userObj = Tools.getUserFromJWTHeader(req);

      Community coIn = Community.fromJson(req.body());

      Community co_ = Actions.saveCommunity(userObj.getId(), coIn);

      res.status(HttpStatus.OK_200);

      return co_.json();

    });

    get("/favorite_communities", (req, res) -> {

      User userObj = Tools.getUserFromJWTHeader(req);

      LazyList<Tables.CommunityUserView> favs = Tables.CommunityUserView.where(
          "user_id = ? and deleted = ? and community_role_id != ?", userObj.getId(), false,
          CommunityRole.BLOCKED.getVal());

      Set<Long> favCommunityIds = favs.collectDistinct("community_id");

      String json = "";
      if (favCommunityIds.size() > 0) {
        LazyList<Tables.CommunityNoTextView> dntv = Tables.CommunityNoTextView
            .where("id in " + Tools.convertListToInQuery(favCommunityIds));

        Communities d = Communities.create(dntv, null, null, null, Long.valueOf(dntv.size()));

        json = d.json();
      } else {
        json = "{\"Communities\": []}";
      }

      return json;

    });

    post("/favorite_community/:id", (req, res) -> {

      User userObj = Tools.getUserFromJWTHeader(req);

      Long communityId = Long.valueOf(req.params(":id"));

      Actions.saveFavoriteCommunity(userObj.getId(), communityId);

      res.status(HttpStatus.OK_200);

      return "{}";

    });

    delete("/favorite_community/:id", (req, res) -> {

      User userObj = Tools.getUserFromJWTHeader(req);

      Long communityId = Long.valueOf(req.params(":id"));

      Actions.deleteFavoriteCommunity(userObj.getId(), communityId);

      res.status(HttpStatus.OK_200);

      return "{}";

    });

    get("/community_modlog/:id", (req, res) -> {

      User userObj = Tools.getUserFromJWTHeader(req);

      Long id = Long.valueOf(req.params(":id"));

      LazyList<Tables.CommunityAuditView> auditRows = Tables.CommunityAuditView.find("community_id = ?", id);

      String json = auditRows.toJson(false, "action", "action_tstamp", "community_id", "discussion_id",
          "discussion_title", "id", "modified_by_user_id", "modified_by_user_name", "table_name", "user_id",
          "user_name", "role_id");

      return json;

    });

  }

}
