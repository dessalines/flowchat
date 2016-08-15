package com.chat.webservice;

import ch.qos.logback.classic.Logger;
import com.chat.DataSources;
import com.chat.db.Actions;
import com.chat.db.Tables;
import com.chat.db.Transformations;
import com.chat.tools.Tools;
import com.chat.types.*;
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

            UserObj userObj = Actions.getOrCreateUserObj(req, res);

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

            TagObj to = TagObj.create(t);

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

            TagObj to = Actions.createTag(name);

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

            UserObj userObj = Actions.getOrCreateUserObj(req, res);

            Tables.DiscussionFullView dfv = Tables.DiscussionFullView.findFirst("id = ?", id);

            // Get your vote for the discussion:
            Tables.DiscussionRank dr = Tables.DiscussionRank.findFirst(
                    "discussion_id = ? and user_id = ?", id, userObj.getId());

            Integer vote = (dr != null) ? dr.getInteger("rank") : null;

            DiscussionObj df = DiscussionObj.create(dfv, vote);

            // check to make sure user is entitled to view it
            df.checkPrivate(userObj);

            // Check to make sure user isn't blocked
            df.checkBlocked(userObj);

            log.info(df.json());

            return df.json();


        });

        // Get the user id
        get("/discussions/:tagId/:limit/:page/:orderBy", (req, res) -> {


            Long tagId = (!req.params(":tagId").equals("all")) ? Long.valueOf(req.params(":tagId")) : null;
            Integer limit = (req.params(":limit") != null) ? Integer.valueOf(req.params(":limit")) : 10;
            Integer page = (req.params(":page") != null) ? Integer.valueOf(req.params(":page")) : 1;
            String orderBy = (req.params(":orderBy") != null) ? req.params(":orderBy") : "time-3600";

            orderBy = Tools.constructOrderByCustom(orderBy);
            UserObj userObj = Actions.getOrCreateUserObj(req, res);

            Paginator p;

            // TODO for now don't show where private is false
            if (tagId != null) {
                p = new Paginator(Tables.DiscussionNoTextView.class, limit, "tag_ids @> ARRAY[?]::bigint[] " +
                        "and private is false and deleted is false and title != ?",
                        tagId, "A new discussion").
                        orderBy(orderBy);
            } else {
                p = new Paginator(Tables.DiscussionNoTextView.class, limit, "private is false and deleted is false and title != ?",
                        "A new discussion").
                        orderBy(orderBy);
            }


            LazyList<Tables.DiscussionNoTextView> dntvs = p.getPage(page);

            // Get the list of discussions
            Set<Long> ids = dntvs.collectDistinct("id");

            // Get your votes for those discussions:
            Map<Long, Integer> discussionRankMap = (!ids.isEmpty()) ? Transformations.convertDiscussionRankToMap(ids, userObj) : null;

            // Build discussion objects
            Discussions discussions = Discussions.create(dntvs, discussionRankMap, p.getCount());

            return discussions.json();

        });

        get("/discussion_search/:query", (req, res) -> {

            String query = req.params(":query");

            String queryStr = Tools.constructQueryString(query, "title");

            LazyList<Tables.DiscussionNoTextView> discussionsRows =
                    Tables.DiscussionNoTextView.find(queryStr.toString()).limit(5);

            Discussions discussions = Discussions.create(discussionsRows, null, Long.valueOf(discussionsRows.size()));

            return discussions.json();

        });

        post("/discussion_rank/:id/:rank", (req, res) -> {

            UserObj userObj = Actions.getOrCreateUserObj(req, res);


            Long discussionId = Long.valueOf(req.params(":id"));
            Integer rank = Integer.valueOf(req.params(":rank"));

            Actions.saveDiscussionVote(userObj.getId(), discussionId, rank);

            res.status(HttpStatus.OK_200);

            return "";

        });

        post("/discussion", (req, res) -> {

            UserObj userObj = Actions.getOrCreateUserObj(req, res);

            DiscussionObj do_ = Actions.createDiscussion(userObj.getId());

            res.status(HttpStatus.CREATED_201);

            return do_.json();

        });

        put("/discussion", (req, res) -> {

            UserObj userObj = Actions.getOrCreateUserObj(req, res);

            DiscussionObj doIn = DiscussionObj.fromJson(req.body());

            DiscussionObj do_ = Actions.saveDiscussion(doIn);

            res.status(HttpStatus.OK_200);

            return do_.json();

        });

        get("/favorite_discussions", (req, res) -> {

            UserObj userObj = Actions.getOrCreateUserObj(req, res);

            LazyList<Tables.FavoriteDiscussionUser> favs = Tables.FavoriteDiscussionUser.where("user_id = ?", userObj.getId());

            Set<Long> favDiscussionIds = favs.collectDistinct("discussion_id");

            String json = "";
            if (favDiscussionIds.size() > 0) {
                LazyList<Tables.DiscussionNoTextView> dntv = Tables.DiscussionNoTextView.where(
                        "id in " + Tools.convertListToInQuery(favDiscussionIds));

                Discussions d = Discussions.create(dntv, null, Long.valueOf(dntv.size()));

                log.info(d.json());

                json = d.json();
            } else {
                json = "{\"Discussions\": []}";
            }

            log.info(json);

            return json;

        });

        post("/favorite_discussion/:id", (req, res) -> {

            UserObj userObj = Actions.getOrCreateUserObj(req, res);

            Long discussionId = Long.valueOf(req.params(":id"));

            Actions.deleteFavoriteDiscussion(userObj.getId(), discussionId);

            res.status(HttpStatus.OK_200);

            return "";

        });
    }

    public static void reply() {

        get("/unread_replies", (req, res) -> {

                UserObj userObj = Actions.getOrCreateUserObj(req, res);

                // Fetch your unread replies
                LazyList<Tables.CommentBreadcrumbsView> cbv = Tables.CommentBreadcrumbsView.where(
                        "parent_user_id = ? and user_id != ? and read = false",
                        userObj.getId(), userObj.getId());

                Comments comments = Comments.replies(cbv);

                log.info(comments.json());

                return comments.json();

        });

        post("/mark_reply_as_read/:id", (req, res) -> {

            UserObj userObj = Actions.getOrCreateUserObj(req, res);

            Long commentId = Long.valueOf(req.params(":id"));

            // Mark the reply as read
            Actions.markReplyAsRead(commentId);

            res.status(HttpStatus.OK_200);

            return "";

        });

        post("/mark_all_replies_as_read", (req, res) -> {

            UserObj userObj = Actions.getOrCreateUserObj(req, res);

            // Mark the reply as read
            Actions.markAllRepliesAsRead(userObj.getId());

            res.status(HttpStatus.OK_200);

            return "";

        });
    }

}
