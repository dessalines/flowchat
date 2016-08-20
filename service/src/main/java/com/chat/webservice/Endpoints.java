package com.chat.webservice;

import ch.qos.logback.classic.Logger;
import com.chat.DataSources;
import com.chat.db.Actions;
import com.chat.db.Tables;
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
import com.chat.types.user.Users;
import org.eclipse.jetty.http.HttpStatus;
import org.javalite.activejdbc.LazyList;
import org.javalite.activejdbc.Paginator;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

import static spark.Spark.*;

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
            res.header("Content-Encoding", "gzip");
            res.header("Access-Control-Allow-Credentials", "true");
            res.header("Access-Control-Allow-Headers", "content-type,user");
            res.header("Access-Control-Allow-Methods", "POST, GET, PUT, DELETE, OPTIONS");
            Tools.dbInit();
        });

        after((req, res) -> {
            Tools.dbClose();
        });

    }

    public static void user() {

        get("/user", (req, res) -> {

            User userObj = Actions.getOrCreateUserObj(req, res);

            return userObj.json();

        });

        post("/login", (req, res) -> {

            log.info(req.body());

            Map<String, String> vars = Tools.createMapFromReqBody(req.body());

            String userOrEmail = vars.get("usernameOrEmail");
            String password = vars.get("password");

            Tables.UserLoginView ulv = Actions.login(userOrEmail, password, req, res);

            return ulv.toJson(false);

        });

        post("/signup", (req, res) -> {

            log.info(req.body());

            Map<String, String> vars = Tools.createMapFromReqBody(req.body());

            String userName = vars.get("username");
            String password = vars.get("password");
            String email = vars.get("email");

            Tables.UserLoginView ulv = Actions.signup(userName, password, email, req, res);

            res.status(HttpStatus.CREATED_201);

            return ulv.toJson(false);

        });

        get("/user_search/:query", (req, res) -> {

            String query = req.params(":query");

            String queryStr = Tools.constructQueryString(query, "name");

            LazyList<Tables.User> userRows = Tables.User.find(queryStr.toString()).limit(5);

            Users users = Users.create(userRows);

            return users.json();

        });

    }

    public static void exceptions() {

        exception(Exception.class, (e, req, res) -> {
            e.printStackTrace();
            res.status(HttpStatus.BAD_REQUEST_400);
            res.body(e.getMessage());
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

            orderBy = Tools.constructOrderByPopularTagsCustom(orderBy);

            Paginator p = new Paginator(Tables.TagsView.class, limit, "1=1").
                    orderBy(orderBy);

            LazyList<Tables.TagsView> popularTags = p.getPage(page);

            return popularTags.toJson(false);

        });

    }

    public static void discussion() {

        get("/discussion/:id", (req, res) -> {


            Long id = Long.valueOf(req.params(":id"));
            log.info("got to discussion " + id);

            User userObj = Actions.getOrCreateUserObj(req, res);

            Tables.DiscussionFullView dfv = Tables.DiscussionFullView.findFirst("id = ?", id);

            // Get your vote for the discussion:
            Tables.DiscussionRank dr = Tables.DiscussionRank.findFirst(
                    "discussion_id = ? and user_id = ?", id, userObj.getId());

            Integer vote = (dr != null) ? dr.getInteger("rank") : null;

            // Get the tags for those discussions:
            LazyList<Tables.DiscussionTagView> tags = Tables.DiscussionTagView.where("discussion_id = ?", id);

            // Get the users for those discussions
            LazyList<Tables.DiscussionUserView> users = Tables.DiscussionUserView.where("discussion_id = ?", id);

            Tables.CommunityNoTextView community = Tables.CommunityNoTextView.findFirst("id = ?", dfv.getLong("community_id"));

            Discussion df = Discussion.create(dfv, community, tags, users, vote);

            // check to make sure user is entitled to view it
            df.checkPrivate(userObj);

            // Check to make sure user isn't blocked
            df.checkBlocked(userObj);

            log.info(df.json());

            return df.json();


        });

        // Get the user id
        get("/discussions/:tagId/:communityId/:limit/:page/:orderBy", (req, res) -> {


            Long tagId = (!req.params(":tagId").equals("all")) ? Long.valueOf(req.params(":tagId")) : null;
            Long communityId = (!req.params(":communityId").equals("all")) ? Long.valueOf(req.params(":communityId")) : null;
            Integer limit = (req.params(":limit") != null) ? Integer.valueOf(req.params(":limit")) : 10;
            Integer page = (req.params(":page") != null) ? Integer.valueOf(req.params(":page")) : 1;
            String orderBy = (req.params(":orderBy") != null) ? req.params(":orderBy") : "time-" + ConstantsService.INSTANCE.getRankingConstants().getCreatedWeight();

            orderBy = Tools.constructOrderByCustom(orderBy);
            User userObj = Actions.getOrCreateUserObj(req, res);

            Paginator p;

            // TODO for now don't show where private is false
            if (tagId != null) {
                if (communityId != null) {
                    p = new Paginator(Tables.DiscussionNoTextView.class, limit, "tag_ids @> ARRAY[?]::bigint[] " +
                            "and community_id = ? " +
                            "and private is false and deleted is false and title != ?",
                            tagId,
                            communityId,
                            "A new discussion").
                            orderBy(orderBy);
                } else {
                    p = new Paginator(Tables.DiscussionNoTextView.class, limit, "tag_ids @> ARRAY[?]::bigint[] " +
                            "and private is false and deleted is false and title != ?",
                            tagId, "A new discussion").
                            orderBy(orderBy);
                }

            } else {
                if (communityId != null) {
                    p = new Paginator(Tables.DiscussionNoTextView.class, limit,
                            "community_id = ? " +
                            "and private is false and deleted is false and title != ?",
                            communityId,
                            "A new discussion").
                            orderBy(orderBy);
                } else {
                    p = new Paginator(Tables.DiscussionNoTextView.class, limit, "private is false and deleted is false and title != ?",
                            "A new discussion").
                            orderBy(orderBy);
                }
            }


            LazyList<Tables.DiscussionNoTextView> dntvs = p.getPage(page);

            Discussions discussions = null;
            if (!dntvs.isEmpty()) {

                // Get the list of discussions
                Set<Long> ids = dntvs.collectDistinct("id");

                // Get a list of the communities
                Set<Long> communityIds = dntvs.collectDistinct("community_id");

                // Get your votes for those discussions:
                LazyList<Tables.DiscussionRank> votes = Tables.DiscussionRank.where(
                        "discussion_id in " + Tools.convertListToInQuery(ids) + " and user_id = ?",
                        userObj.getId());

                // Get the tags for those discussions:
                LazyList<Tables.DiscussionTagView> tags = Tables.DiscussionTagView.where(
                        "discussion_id in " + Tools.convertListToInQuery(ids));

                // Get the users for those discussions
                LazyList<Tables.DiscussionUserView> users = Tables.DiscussionUserView.where(
                        "discussion_id in " + Tools.convertListToInQuery(ids));

                // Get the communities for those discussions
                LazyList<Tables.CommunityNoTextView> communities = Tables.CommunityNoTextView.where(
                        "id in " + Tools.convertListToInQuery(communityIds));

                // Build discussion objects
                discussions = Discussions.create(dntvs, communities, tags, users, votes, p.getCount());

            } else {
                discussions = Discussions.create(dntvs, null, null, null, null, p.getCount());
            }

            return discussions.json();

        });

        get("/discussion_search/:query", (req, res) -> {

            String query = req.params(":query");

            String queryStr = Tools.constructQueryString(query, "title");

            LazyList<Tables.DiscussionNoTextView> discussionsRows =
                    Tables.DiscussionNoTextView.find("deleted is false and " + queryStr.toString()).limit(5);

            Discussions discussions = Discussions.create(discussionsRows, null, null, null, null, Long.valueOf(discussionsRows.size()));

            return discussions.json();

        });

        post("/discussion_rank/:id/:rank", (req, res) -> {

            User userObj = Actions.getOrCreateUserObj(req, res);


            Long discussionId = Long.valueOf(req.params(":id"));
            Integer rank = Integer.valueOf(req.params(":rank"));

            Actions.saveDiscussionVote(userObj.getId(), discussionId, rank);

            res.status(HttpStatus.OK_200);

            return "";

        });

        post("/discussion", (req, res) -> {

            User userObj = Actions.getOrCreateUserObj(req, res);

            Discussion do_ = Actions.createDiscussion(userObj.getId());

            res.status(HttpStatus.CREATED_201);

            return do_.json();

        });

        put("/discussion", (req, res) -> {

            User userObj = Actions.getOrCreateUserObj(req, res);

            Discussion doIn = Discussion.fromJson(req.body());

            Discussion do_ = Actions.saveDiscussion(doIn);

            res.status(HttpStatus.OK_200);

            return do_.json();

        });

        get("/favorite_discussions", (req, res) -> {

            User userObj = Actions.getOrCreateUserObj(req, res);

            LazyList<Tables.FavoriteDiscussionUser> favs = Tables.FavoriteDiscussionUser.where("user_id = ?", userObj.getId());

            Set<Long> favDiscussionIds = favs.collectDistinct("discussion_id");

            String json = "";
            if (favDiscussionIds.size() > 0) {
                LazyList<Tables.DiscussionNoTextView> dntv = Tables.DiscussionNoTextView.where(
                        "id in " + Tools.convertListToInQuery(favDiscussionIds));

                Discussions d = Discussions.create(dntv, null, null, null, null, Long.valueOf(dntv.size()));

                log.info(d.json());

                json = d.json();
            } else {
                json = "{\"Discussions\": []}";
            }

            log.info(json);

            return json;

        });

        delete("/favorite_discussion/:id", (req, res) -> {

            User userObj = Actions.getOrCreateUserObj(req, res);

            Long discussionId = Long.valueOf(req.params(":id"));

            Actions.deleteFavoriteDiscussion(userObj.getId(), discussionId);

            res.status(HttpStatus.OK_200);

            return "";

        });
    }

    public static void reply() {

        get("/unread_replies", (req, res) -> {

                User userObj = Actions.getOrCreateUserObj(req, res);

                // Fetch your unread replies
                LazyList<Tables.CommentBreadcrumbsView> cbv = Tables.CommentBreadcrumbsView.where(
                        "parent_user_id = ? and user_id != ? and read = false",
                        userObj.getId(), userObj.getId());

                Comments comments = Comments.replies(cbv);

                log.info(comments.json());

                return comments.json();

        });

        post("/mark_reply_as_read/:id", (req, res) -> {

            User userObj = Actions.getOrCreateUserObj(req, res);

            Long commentId = Long.valueOf(req.params(":id"));

            // Mark the reply as read
            Actions.markReplyAsRead(commentId);

            res.status(HttpStatus.OK_200);

            return "";

        });

        post("/mark_all_replies_as_read", (req, res) -> {

            User userObj = Actions.getOrCreateUserObj(req, res);

            // Mark the reply as read
            Actions.markAllRepliesAsRead(userObj.getId());

            res.status(HttpStatus.OK_200);

            return "";

        });
    }

    public static void community() {

        get("/community/:id", (req, res) -> {

            Long id = Long.valueOf(req.params(":id"));

            User userObj = Actions.getOrCreateUserObj(req, res);

            Tables.CommunityView cv = Tables.CommunityView.findFirst("id = ?", id);

            // Get your vote for the community:
            Tables.CommunityRank cr = Tables.CommunityRank.findFirst(
                    "community_id = ? and user_id = ?", id, userObj.getId());

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

            log.info(co.json());

            return co.json();

        });

        // Get the user id
        get("/communities/:tagId/:limit/:page/:orderBy", (req, res) -> {

            Long tagId = (!req.params(":tagId").equals("all")) ? Long.valueOf(req.params(":tagId")) : null;
            Integer limit = (req.params(":limit") != null) ? Integer.valueOf(req.params(":limit")) : 10;
            Integer page = (req.params(":page") != null) ? Integer.valueOf(req.params(":page")) : 1;

            String orderBy = (req.params(":orderBy") != null) ? req.params(":orderBy") : "time-" + ConstantsService.INSTANCE.getRankingConstants().getCreatedWeight();;

            orderBy = Tools.constructOrderByCustom(orderBy);
            User userObj = Actions.getOrCreateUserObj(req, res);

            Paginator p;

            // TODO for now don't show where private is false
            if (tagId != null) {
                // fetch the tags
                p = new Paginator(Tables.CommunityNoTextView.class, limit, "tag_ids @> ARRAY[?]::bigint[] " +
                        "and private is false and deleted is false and name not like ?",
                        tagId, "new_community%").
                        orderBy(orderBy);
            } else {
                p = new Paginator(Tables.CommunityNoTextView.class, limit, "private is false and deleted is false and name not like ?",
                        "new_community%").
                        orderBy(orderBy);
            }


            LazyList<Tables.CommunityNoTextView> cv = p.getPage(page);

            Communities communities;
            if (!cv.isEmpty()) {
                // Get the list of communities
                Set<Long> ids = cv.collectDistinct("id");

                // Get your votes for those communities:
                LazyList<Tables.CommunityRank> votes = Tables.CommunityRank.where(
                        "community_id in " + Tools.convertListToInQuery(ids) + " and user_id = ?",
                        userObj.getId());

                // Get the tags for those communities:
                LazyList<Tables.CommunityTagView> tags = Tables.CommunityTagView.where(
                        "community_id in " + Tools.convertListToInQuery(ids));

                // Get the users for those communities
                LazyList<Tables.CommunityUserView> users = Tables.CommunityUserView.where(
                        "community_id in " + Tools.convertListToInQuery(ids));

                // Build community objects
                communities = Communities.create(cv, tags, users, votes, p.getCount());

            } else {
                communities = Communities.create(cv, null, null, null, p.getCount());
            }


            return communities.json();

        });

        get("/community_search/:query", (req, res) -> {

            String query = req.params(":query");

            String queryStr = Tools.constructQueryString(query, "name");

            LazyList<Tables.CommunityNoTextView> communityRows =
                    Tables.CommunityNoTextView.find("deleted is false and " + queryStr.toString()).limit(5);

            Communities communities = Communities.create(communityRows, null, null, null, Long.valueOf(communityRows.size()));

            return communities.json();

        });

        post("/community_rank/:id/:rank", (req, res) -> {

            User userObj = Actions.getOrCreateUserObj(req, res);

            Long id = Long.valueOf(req.params(":id"));
            Integer rank = Integer.valueOf(req.params(":rank"));

            Actions.saveCommunityVote(userObj.getId(), id, rank);

            res.status(HttpStatus.OK_200);

            return "";

        });

        post("/community", (req, res) -> {

            User userObj = Actions.getOrCreateUserObj(req, res);

            Community co_ = Actions.createCommunity(userObj.getId());

            res.status(HttpStatus.CREATED_201);

            return co_.json();

        });

        put("/community", (req, res) -> {

            User userObj = Actions.getOrCreateUserObj(req, res);

            Community coIn = Community.fromJson(req.body());

            Community co_ = Actions.saveCommunity(coIn);

            res.status(HttpStatus.OK_200);

            return co_.json();

        });

        get("/favorite_communities", (req, res) -> {

            User userObj = Actions.getOrCreateUserObj(req, res);

            LazyList<Tables.CommunityUser> favs = Tables.CommunityUser.where("user_id = ?",
                    userObj.getId());

            Set<Long> favCommunityIds = favs.collectDistinct("community_id");

            String json = "";
            if (favCommunityIds.size() > 0) {
                LazyList<Tables.CommunityNoTextView> dntv = Tables.CommunityNoTextView.where(
                        "id in " + Tools.convertListToInQuery(favCommunityIds));

                Communities d = Communities.create(dntv, null, null, null, Long.valueOf(dntv.size()));

                log.info(d.json());

                json = d.json();
            } else {
                json = "{\"Communities\": []}";
            }

            log.info(json);

            return json;

        });

        post("/favorite_community/:id", (req, res) -> {

            User userObj = Actions.getOrCreateUserObj(req, res);

            Long communityId = Long.valueOf(req.params(":id"));

            Actions.saveFavoriteCommunity(userObj.getId(), communityId);

            res.status(HttpStatus.OK_200);

            return "";

        });

        delete("/favorite_community/:id", (req, res) -> {

            User userObj = Actions.getOrCreateUserObj(req, res);

            Long discussionId = Long.valueOf(req.params(":id"));

            Actions.deleteFavoriteCommunity(userObj.getId(), discussionId);

            res.status(HttpStatus.OK_200);

            return "";

        });

    }

}
