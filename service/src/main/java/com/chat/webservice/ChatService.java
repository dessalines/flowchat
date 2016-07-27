package com.chat.webservice;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.chat.DataSources;
import com.chat.db.Actions;
import com.chat.db.Transformations;
import com.chat.tools.Tools;
import com.chat.types.*;
import org.javalite.activejdbc.LazyList;
import org.javalite.activejdbc.Paginator;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.chat.db.Tables.*;
import static spark.Spark.*;

public class ChatService {

    static Logger log = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

    public static void main(String[] args) {

        log.setLevel(Level.toLevel("verbose"));
        log.getLoggerContext().getLogger("org.eclipse.jetty").setLevel(Level.OFF);
        log.getLoggerContext().getLogger("spark.webserver").setLevel(Level.OFF);

        staticFiles.externalLocation("../ui/dist");
        staticFiles.expireTime(600);

        // Instantiates the ranking constants
        ConstantsService.INSTANCE.getRankingConstants();

        webSocket("/threaded_chat", ThreadedChatWebSocket.class);

        get("/version", (req, res) -> {
            return "{\"version\":\"" + DataSources.PROPERTIES.getProperty("version") + "\"}";
        });

        // Get the user id
        get("/get_user", (req, res) -> {

            try {

                UserObj userObj = Actions.getOrCreateUserObj(req, res);

                return userObj.json();

            } catch (Exception e) {
                res.status(666);
                e.printStackTrace();
                return Tools.buildMessage(e.getMessage());
            }

        });

        get("/get_tag/:id", (req, res) -> {

            Long id = Long.valueOf(req.params(":id"));

            try {
                Tag t = Tag.findFirst("id = ?", id);

                TagObj to = TagObj.create(t);

                return to.json();

            } catch (Exception e) {
                res.status(666);
                e.printStackTrace();
                return Tools.buildMessage(e.getMessage());
            }

        });

        post("/login", (req, res) -> {
            try {
                log.info(req.body());

                Map<String, String> vars = Tools.createMapFromReqBody(req.body());

                String userOrEmail = vars.get("usernameOrEmail");
                String password = vars.get("password");

                UserLoginView ulv = Actions.login(userOrEmail, password, req, res);

                return ulv.toJson(false);

            } catch (Exception e) {
                res.status(666);
                e.printStackTrace();
                return Tools.buildMessage(e.getMessage());
            }

        });

        post("/signup", (req, res) -> {
            try {

                log.info(req.body());

                Map<String, String> vars = Tools.createMapFromReqBody(req.body());

                String userName = vars.get("username");
                String password = vars.get("password");
                String email = vars.get("email");

                UserLoginView ulv = Actions.signup(userName, password, email, req, res);

                return ulv.toJson(false);

            } catch (Exception e) {
                res.status(666);
                e.printStackTrace();
                return Tools.buildMessage(e.getMessage());
            }

        });

        get("/get_discussion/:id", (req, res) -> {

            try {
                Long id = Long.valueOf(req.params(":id"));
                log.info("got to discussion " + id);

                UserObj userObj = Actions.getOrCreateUserObj(req, res);

                DiscussionFullView dfv = DiscussionFullView.findFirst("id = ?", id);

                // Get your vote for the discussion:
                DiscussionRank dr = DiscussionRank.findFirst(
                        "discussion_id = ? and user_id = ?", id, userObj.getId());

                Integer vote = (dr != null) ? dr.getInteger("rank") : null;

                DiscussionObj df = DiscussionObj.create(dfv, vote);

                // check to make sure user is entitled to view it
                df.checkPrivate(userObj);

                // Check to make sure user isn't blocked
                df.checkBlocked(userObj);

                log.info(df.json());

                return df.json();

            } catch (Exception e) {
                res.status(666);
                e.printStackTrace();
                return Tools.buildMessage(e.getMessage());
            }

        });

        // Get the user id
        get("/get_discussions/:tagId/:limit/:page/:orderBy", (req, res) -> {

            try {
                Long tagId = (!req.params(":tagId").equals("all")) ? Long.valueOf(req.params(":tagId")) : null;
                Integer limit = (req.params(":limit") != null) ? Integer.valueOf(req.params(":limit")) : 10;
                Integer page = (req.params(":page") != null) ? Integer.valueOf(req.params(":page")) : 1;
                String orderBy = (req.params(":orderBy") != null) ? req.params(":orderBy") : "time-3600";

                orderBy = constructOrderByCustom(orderBy);
                UserObj userObj = Actions.getOrCreateUserObj(req, res);

                Paginator p;

                // TODO for now don't show where private is false
                if (tagId != null) {
                    p = new Paginator(DiscussionNoTextView.class, limit, "tag_ids @> ARRAY[?]::bigint[] " +
                            "and private is false and deleted is false and title != ?",
                            tagId, "A new discussion").
                            orderBy(orderBy);
                } else {
                    p = new Paginator(DiscussionNoTextView.class, limit, "private is false and deleted is false and title != ?",
                            "A new discussion").
                            orderBy(orderBy);
                }


                LazyList<DiscussionNoTextView> dntvs = p.getPage(page);

                // Get the list of discussions
                Set<Long> ids = dntvs.collectDistinct("id");

                // Get your votes for those discussions:
                Map<Long, Integer> discussionRankMap = (!ids.isEmpty()) ? Transformations.convertDiscussionRankToMap(ids, userObj) : null;

                // Build discussion objects
                Discussions discussions = Discussions.create(dntvs, discussionRankMap, p.getCount());

                return discussions.json();

            } catch (Exception e) {
                res.status(666);
                e.printStackTrace();
                return Tools.buildMessage(e.getMessage());
            }

        });

        get("/discussion_search/:query", (req, res) -> {

            try {

                String query = req.params(":query");

                String queryStr = Tools.constructQueryString(query, "title");

                LazyList<DiscussionNoTextView> discussionsRows =
                        DiscussionNoTextView.find(queryStr.toString()).limit(5);

                Discussions discussions = Discussions.create(discussionsRows, null, Long.valueOf(discussionsRows.size()));

                return discussions.json();

            } catch (Exception e) {
                res.status(666);
                e.printStackTrace();
                return Tools.buildMessage(e.getMessage());
            }


        });

        post("/save_discussion_rank/:id/:rank", (req, res) -> {
            try {
                UserObj userObj = Actions.getOrCreateUserObj(req, res);


                Long discussionId = Long.valueOf(req.params(":id"));
                Integer rank = Integer.valueOf(req.params(":rank"));

                String message = Actions.saveDiscussionVote(userObj.getId(), discussionId, rank);

                return Tools.buildMessage(message);

            } catch (Exception e) {
                res.status(666);
                e.printStackTrace();
                return Tools.buildMessage(e.getMessage());
            }

        });

        post("/create_discussion", (req, res) -> {
            try {
                UserObj userObj = Actions.getOrCreateUserObj(req, res);

                DiscussionObj do_ = Actions.createDiscussion(userObj.getId());

                return do_.json();

            } catch (Exception e) {
                res.status(666);
                e.printStackTrace();
                return Tools.buildMessage(e.getMessage());
            }

        });

        post("/save_discussion", (req, res) -> {
            try {
                UserObj userObj = Actions.getOrCreateUserObj(req, res);

//                Map<String, String> m = Tools.createMapFromReqBody(req.body());

                DiscussionObj doIn = DiscussionObj.fromJson(req.body());

                DiscussionObj do_ = Actions.saveDiscussion(doIn);

                return do_.json();

            } catch (Exception e) {
                res.status(666);
                e.printStackTrace();
                return Tools.buildMessage(e.getMessage());
            }

        });

        get("/get_favorite_discussions", (req, res) -> {
            try {
                UserObj userObj = Actions.getOrCreateUserObj(req, res);

                LazyList<FavoriteDiscussionUser> favs = FavoriteDiscussionUser.where("user_id = ?", userObj.getId());

                Set<Long> favDiscussionIds = favs.collectDistinct("discussion_id");

                String json = "";
                if (favDiscussionIds.size() > 0) {
                    LazyList<DiscussionNoTextView> dntv = DiscussionNoTextView.where(
                            "id in " + Tools.convertListToInQuery(favDiscussionIds));

                    Discussions d = Discussions.create(dntv, null, Long.valueOf(dntv.size()));

                    log.info(d.json());

                    json = d.json();
                } else {
                    json = "{\"Discussions\": []}";
                }

                log.info(json);

                return json;

            } catch (Exception e) {
                res.status(666);
                e.printStackTrace();
                return Tools.buildMessage(e.getMessage());
            }

        });

        post("/remove_favorite_discussion/:id", (req, res) -> {
            try {
                UserObj userObj = Actions.getOrCreateUserObj(req, res);

                Long discussionId = Long.valueOf(req.params(":id"));

                String message = Actions.deleteFavoriteDiscussion(userObj.getId(), discussionId);

                return message;

            } catch (Exception e) {
                res.status(666);
                e.printStackTrace();
                return Tools.buildMessage(e.getMessage());
            }

        });

        get("/tag_search/:query", (req, res) -> {

            try {

                String query = req.params(":query");

                String queryStr = Tools.constructQueryString(query, "name");

                LazyList<Tag> tagRows = Tag.find(queryStr.toString()).limit(5);

                Tags tags = Tags.create(tagRows);

                return tags.json();

            } catch (Exception e) {
                res.status(666);
                e.printStackTrace();
                return Tools.buildMessage(e.getMessage());
            }


        });

        post("/create_tag", (req, res) -> {
            try {

                String name = Tools.createMapFromReqBody(req.body()).get("name");

                TagObj to = Actions.createTag(name);

                return to.json();

            } catch (Exception e) {
                res.status(666);
                e.printStackTrace();
                return Tools.buildMessage(e.getMessage());
            }

        });

        get("/user_search/:query", (req, res) -> {

            try {

                String query = req.params(":query");

                String queryStr = Tools.constructQueryString(query, "name");

                LazyList<User> userRows = User.find(queryStr.toString()).limit(5);

                Users users = Users.create(userRows);

                return users.json();

            } catch (Exception e) {
                res.status(666);
                e.printStackTrace();
                return Tools.buildMessage(e.getMessage());
            }


        });

        get("/get_popular_tags/:limit/:page/:orderBy", (req, res) -> {

            try {

                Integer limit = (req.params(":limit") != null) ? Integer.valueOf(req.params(":limit")) : 10;
                Integer page = (req.params(":page") != null) ? Integer.valueOf(req.params(":page")) : 1;
                String orderBy = (req.params(":orderBy") != null) ? req.params(":orderBy") : "custom";

                orderBy = constructOrderByPopularTagsCustom(orderBy);

                Paginator p = new Paginator(PopularTagsView.class, limit, "1=1").
                        orderBy(orderBy);

                LazyList<PopularTagsView> popularTags = p.getPage(page);

                return popularTags.toJson(false);

            } catch (Exception e) {
                res.status(666);
                e.printStackTrace();
                return Tools.buildMessage(e.getMessage());
            }


        });

        get("/get_unread_replies", (req, res) -> {

            try {

                UserObj userObj = Actions.getOrCreateUserObj(req, res);

                // Fetch your unread replies
                LazyList<CommentBreadcrumbsView> cbv = CommentBreadcrumbsView.where(
                        "parent_user_id = ? and user_id != ? and read = false",
                        userObj.getId(), userObj.getId());

                Comments comments = Comments.replies(cbv);

                log.info(comments.json());

                return comments.json();

            } catch (Exception e) {
                res.status(666);
                e.printStackTrace();
                return Tools.buildMessage(e.getMessage());
            }

        });

        post("/mark_reply_as_read/:id", (req, res) -> {
            try {
                UserObj userObj = Actions.getOrCreateUserObj(req, res);

                Long commentId = Long.valueOf(req.params(":id"));

                // Mark the reply as read
                String message = Actions.markReplyAsRead(commentId);

                return message;

            } catch (Exception e) {
                res.status(666);
                e.printStackTrace();
                return Tools.buildMessage(e.getMessage());
            }

        });

        post("/mark_all_replies_as_read", (req, res) -> {
            try {
                UserObj userObj = Actions.getOrCreateUserObj(req, res);


                // Mark the reply as read
                String message = Actions.markAllRepliesAsRead(userObj.getId());

                return message;

            } catch (Exception e) {
                res.status(666);
                e.printStackTrace();
                return Tools.buildMessage(e.getMessage());
            }

        });

        before((req, res) -> {
            Tools.dbInit();
        });

        after((req, res) -> {
            Tools.dbClose();
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Content-Encoding", "gzip");
            res.header("Access-Control-Allow-Credentials", "true");
            res.header("Access-Control-Allow-Headers", "content-type,user");

        });

        init();

    }


    public static String constructOrderByCustom(String orderBy) {

        String orderByOut;
        if (orderBy.startsWith("time-")) {
            Long timeValue = Long.valueOf(orderBy.split("-")[1]);

            // For the custom sorting based on ranking
            orderByOut = "ranking(created, " + timeValue +
                    ",number_of_votes, " + ConstantsService.INSTANCE.getRankingConstants().getNumberOfVotesWeight() +
                    ",avg_rank, " + ConstantsService.INSTANCE.getRankingConstants().getAvgRankWeight() +
                    ") desc nulls last";

        } else {
            orderByOut = orderBy.replaceAll("__", " ").concat(" nulls last");
        }

        return orderByOut;
    }

    public static String constructOrderByPopularTagsCustom(String orderBy) {
        // For the custom sorting based on ranking
        if (orderBy.equals("custom")) {
            orderBy = "ranking(created, " + ConstantsService.INSTANCE.getRankingConstants().getCreatedWeight() +
                    ",count, " + ConstantsService.INSTANCE.getRankingConstants().getNumberOfVotesWeight() +
                    ") desc";
        }

        return orderBy;
    }


}
