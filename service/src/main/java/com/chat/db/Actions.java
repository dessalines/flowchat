package com.chat.db;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

import ch.qos.logback.classic.Logger;
import com.chat.DataSources;
import com.chat.db.Tables.*;
import com.chat.db.Tables.Community;
import com.chat.tools.Tools;
import com.chat.types.community.*;
import com.chat.types.community.CommunityRole;
import com.chat.types.discussion.Discussion;
import com.chat.types.tag.Tag;
import com.chat.types.user.User;
import org.javalite.activejdbc.LazyList;
import org.postgresql.util.PSQLException;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

/**
 * Created by tyler on 6/5/16.
 */
public class Actions {

    public static Logger log = (Logger) LoggerFactory.getLogger(Actions.class);

    public static Comment createComment(Long userId, Long discussionId,
                                        List<Long> parentBreadCrumbs, String text) {

        List<Long> pbs = (parentBreadCrumbs != null) ? new ArrayList<Long>(parentBreadCrumbs) :
                new ArrayList<Long>();


        // find the candidate
        Comment c = Comment.createIt("discussion_id" , discussionId,
                "text_" , text,
                "user_id" , userId,
                "modified_by_user_id", userId);


        Long childId = c.getLong("id");

        // This is necessary, because of the 0 path length to itself one
        pbs.add(childId);

        Collections.reverse(pbs);


        // Create the comment_tree
        for (int i = 0; i < pbs.size(); i++) {

            Long parentId = pbs.get(i);

            // i is the path length
            CommentTree.createIt("parent_id" , parentId,
                    "child_id" , childId,
                    "path_length" , i);
        }

        return c;

    }

    public static Comment editComment(Long userId, Long commentId, String text) {

        // Find the comment
        Comment c = Comment.findFirst("id = ?" , commentId);

        Timestamp cTime = new Timestamp(new Date().getTime());

        // Create with add modified date
        c.set("text_" , text,
                "modified" , cTime,
                "modified_by_user_id", userId).saveIt();

        return c;

    }

    public static Comment deleteComment(Long userId, Long commentId) {
        // Find the comment
        Comment c = Comment.findFirst("id = ?" , commentId);

        Timestamp cTime = new Timestamp(new Date().getTime());

        // Create with add modified date
        c.set("deleted" , true,
                "modified" , cTime,
                "modified_by_user_id", userId).saveIt();

        return c;
    }

    public static User getOrCreateUserObj(Long id, String auth) {

        log.info("getOrCreateUser id = " + id + " auth = " + auth);

        User userObj;
        if (id != null) {

            if (auth == null || auth.equals("undefined")) {
                Tables.User dbUser = Tables.User.findFirst("id = ?" , id);
                userObj = User.create(dbUser.getLongId(), dbUser.getString("name"));
            } else {
                UserLoginView uv = UserLoginView.findFirst("auth = ?" , auth);
                userObj = User.create(uv.getLongId(), uv.getString("name"));
            }

        } else {
            Tables.User dbUser = Actions.createUser();
            userObj = User.create(dbUser.getLongId(), dbUser.getString("name"));
        }

        return userObj;
    }

    //  TODO make this more generic, don't require creating login rows for the anonymous users
    public static User getOrCreateUserObj(Request req, Response res) {

        log.info(req.headers("user"));

        UserFromHeader ufh;

        if (req.headers("user") != null) {
            ufh = UserFromHeader.fromJson(req.headers("user"));
        } else {
            ufh = new UserFromHeader(null, null);
        }

        return getOrCreateUserObj(ufh.getId(), ufh.getAuth());

    }

    public static Discussion createDiscussion(Long userId) {

        log.info("Creating discussion");
        String title = "A new discussion";

        Tables.Discussion d = Tables.Discussion.createIt("title", title,
                "modified_by_user_id", userId);

        DiscussionUser.createIt("user_id", userId,
                "discussion_id", d.getLong("id"),
                "discussion_role_id", com.chat.types.discussion.DiscussionRole.CREATOR.getVal());

        FavoriteDiscussionUser.createIt("user_id", userId,
                "discussion_id", d.getLong("id"));

        DiscussionFullView dfv = DiscussionFullView.findFirst("id = ?", d.getLongId());
        List<DiscussionUserView> udv = DiscussionUserView.where("discussion_id = ?", d.getLongId());
        CommunityNoTextView cntv = CommunityNoTextView.findFirst("id = ?", dfv.getLong("community_id"));

        return Discussion.create(dfv, cntv, null, udv, null, null);
    }

    public static Discussion saveDiscussion(Long userId, Discussion do_) {

        Timestamp cTime = new Timestamp(new Date().getTime());

        Tables.Discussion d = Tables.Discussion.findFirst("id = ?" , do_.getId());
        LazyList<DiscussionUserView> udv = DiscussionUserView.where("discussion_id = ?", do_.getId());

        log.info(udv.toJson(true));
        log.info(do_.json());

        if (do_.getTitle() != null) d.set("title" , do_.getTitle());
        if (do_.getLink() != null) d.set("link" , do_.getLink());
        if (do_.getText() != null) d.set("text_" , do_.getText());
        if (do_.getPrivate_() != null) d.set("private" , do_.getPrivate_());
        if (do_.getDeleted() != null) d.set("deleted", do_.getDeleted());
        if (do_.getCommunity() != null) d.set("community_id", do_.getCommunity().getId());

        d.set("modified_by_user_id", userId);
        d.set("modified" , cTime);
        d.saveIt();

        // Add the discussion tags
        if (do_.getTags() != null) {
            DiscussionTag.delete("discussion_id = ?", do_.getId());

            for (Tag tag : do_.getTags()) {
                DiscussionTag.createIt("discussion_id", do_.getId(),
                        "tag_id", tag.getId());
            }
        }

        if (do_.getPrivateUsers() != null) {

            DiscussionUser.delete("discussion_id = ? and discussion_role_id = ?", do_.getId(), com.chat.types.discussion.DiscussionRole.USER.getVal());

            for (User userObj : do_.getPrivateUsers()) {
                DiscussionUser.createIt("discussion_id", do_.getId(),
                        "user_id", userObj.getId(),
                        "discussion_role_id", com.chat.types.discussion.DiscussionRole.USER.getVal());
            }

        }

        if (do_.getBlockedUsers() != null) {

            DiscussionUser.delete("discussion_id = ? and discussion_role_id = ?", do_.getId(), com.chat.types.discussion.DiscussionRole.BLOCKED.getVal());

            for (User userObj : do_.getBlockedUsers()) {
                DiscussionUser.createIt("discussion_id", do_.getId(),
                        "user_id", userObj.getId(),
                        "discussion_role_id", com.chat.types.discussion.DiscussionRole.BLOCKED.getVal());
            }
        }



        // Fetch the full view
        DiscussionFullView dfv = DiscussionFullView.findFirst("id = ?", do_.getId());
        List<DiscussionTagView> dtv = DiscussionTagView.where("discussion_id = ?", do_.getId());
        List<DiscussionUserView> ud = DiscussionUserView.where("discussion_id = ?", do_.getId());
        CommunityNoTextView cntv = CommunityNoTextView.findFirst("id = ?", dfv.getLong("community_id"));
        List<Tables.CommunityUserView> communityUsers = Tables.CommunityUserView.where("community_id = ?", cntv.getLong("id"));

        Discussion doOut = Discussion.create(dfv, cntv, dtv, ud, communityUsers, null);

        return doOut;
    }

    public static Tag createTag(String name) {

        Tables.Tag t = Tables.Tag.createIt("name", name);

        return Tag.create(t);
    }

    public static Discussion saveFavoriteDiscussion(Long userId, Long discussionId) {

        FavoriteDiscussionUser fdu = FavoriteDiscussionUser.findFirst(
                "user_id = ? and discussion_id = ?", userId, discussionId);

        if (fdu == null) {
            FavoriteDiscussionUser.createIt("user_id", userId,
                    "discussion_id", discussionId);

            DiscussionNoTextView dntv = DiscussionNoTextView.findFirst("id = ?", discussionId);

            return Discussion.create(dntv, null, null, null, null, null);
        } else {
            return null;
        }

    }

    public static void deleteFavoriteDiscussion(Long userId, Long discussionId) {

        FavoriteDiscussionUser fdu = FavoriteDiscussionUser.findFirst(
                "user_id = ? and discussion_id = ?", userId, discussionId);

        fdu.delete();

    }

    public static void markReplyAsRead(Long commentId) {

        Comment c = Comment.findFirst("id = ?", commentId);
        c.set("read", true).saveIt();

    }

    public static void markAllRepliesAsRead(Long userId) {

        // Fetch your unread replies
        LazyList<CommentBreadcrumbsView> cbv = CommentBreadcrumbsView.where(
                "parent_user_id = ? and user_id != ? and read = false",
                userId, userId);

        Set<Long> ids = cbv.collectDistinct("id");

        if (ids.size() > 0) {

            String inQuery = Tools.convertListToInQuery(ids);

            Comment.update("read = ?", "id in " + inQuery, true);

        }

    }


    private static class UserFromHeader {
        private Long id, full_user_id, login_id;
        private String auth, name, email;
        private Timestamp created, expire_time;


        public UserFromHeader() {
        }

        public UserFromHeader(Long id, String auth) {
            this.id = id;
            this.auth = auth;
        }

        public static UserFromHeader fromJson(String dataStr) {
            try {
                log.info(dataStr);
                return Tools.JACKSON.readValue(dataStr, UserFromHeader.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        public String getAuth() {
            return auth;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public Long getFull_user_id() {
            return full_user_id;
        }

        public Long getLogin_id() {
            return login_id;
        }

        public String getEmail() {
            return email;
        }

        public Timestamp getCreated() {
            return created;
        }

        public Timestamp getExpire_time() {
            return expire_time;
        }
    }

    public static Tables.User createUser() {
        Tables.User user = Tables.User.createIt(
                "name" , Tools.generateSecureRandom());
        user.set("name" , "user_" + user.getLongId()).saveIt();

        return user;
    }

    public static UserLoginView login(String userOrEmail, String password, Request req, Response res) {

        // Find the user, then create a login for them

        UserView uv = UserView.findFirst("name = ? or email = ?" , userOrEmail, userOrEmail);

        Login login;
        if (uv == null) {
            throw new NoSuchElementException("Incorrect user/email");
        } else {
            FullUser fu = FullUser.findFirst("user_id = ?" , uv.getLongId());

            String encryptedPassword = fu.getString("password_encrypted");

            Boolean correctPass = Tools.PASS_ENCRYPT.checkPassword(password, encryptedPassword);

            if (correctPass) {

                String auth = Tools.generateSecureRandom();
                login = Login.createIt("user_id" , fu.getInteger("user_id"),
                        "auth" , auth,
                        "expire_time" , Tools.newExpireTimestamp());

                Actions.setCookiesForLogin(fu, auth, res);

            } else {
                throw new NoSuchElementException("Incorrect Password");
            }
        }

        UserLoginView ulv = UserLoginView.findFirst("login_id = ?" , login.getLongId());

        return ulv;

    }

    public static UserLoginView signup(String userName, String password, String email, Request req, Response res) {


        // Find the user, then create a login for them

        UserView uv = UserView.findFirst("name = ? or email = ?" , userName, email);

        Login login;

        if (uv == null) {

            // Create the user and full user
            Tables.User user = Tables.User.createIt(
                    "name" , userName);

            log.info("encrypting the user password");
            String encryptedPassword = Tools.PASS_ENCRYPT.encryptPassword(password);

            FullUser fu = FullUser.createIt("user_id" , user.getId(),
                    "email" , email,
                    "password_encrypted" , encryptedPassword);

            // now login that user
            String auth = Tools.generateSecureRandom();
            login = Login.createIt("user_id" , user.getId(),
                    "auth" , auth,
                    "expire_time" , Tools.newExpireTimestamp());

            Actions.setCookiesForLogin(fu, auth, res);

        } else {
            throw new NoSuchElementException("Username/email already exists");
        }

        UserLoginView ulv = UserLoginView.findFirst("login_id = ?" , login.getLongId());

        return ulv;

    }

    public static String saveCommentVote(Long userId, Long commentId, Integer rank) {

        String message = null;
        // fetch the vote if it exists
        CommentRank c = CommentRank.findFirst("user_id = ? and comment_id = ?" ,
                userId, commentId);


        if (c == null) {
            if (rank != null) {
                CommentRank.createIt(
                        "comment_id" , commentId,
                        "user_id" , userId,
                        "rank" , rank);
                message = "Comment Vote Created";
            } else {
                message = "Comment Vote not created";
            }
        } else {
            if (rank != null) {
                c.set("rank" , rank).saveIt();
                message = "Comment Vote updated";
            }
            // If the rank is null, then delete the ballot
            else {
                c.delete();
                message = "Comment Vote deleted";
            }
        }

        return message;

    }

    public static void saveDiscussionVote(Long userId, Long discussionId, Integer rank) {

        // fetch the vote if it exists
        DiscussionRank d = DiscussionRank.findFirst("user_id = ? and discussion_id = ?" ,
                userId, discussionId);

        if (rank != null) {
            if (d == null) {
                DiscussionRank.createIt(
                        "discussion_id", discussionId,
                        "user_id", userId,
                        "rank", rank);
            } else {
                d.set("rank", rank).saveIt();
            }
        }
        // If the rank is null, then delete the ballot
        else {
            d.delete();
        }

    }

    public static void saveCommunityVote(Long userId, Long communityId, Integer rank) {

        // fetch the vote if it exists
        CommunityRank cr = CommunityRank.findFirst("user_id = ? and community_id = ?" ,
                userId, communityId);

        if (rank != null) {
            if (cr == null) {
                CommunityRank.createIt(
                        "community_id", communityId,
                        "user_id", userId,
                        "rank", rank);
            } else {
                cr.set("rank", rank).saveIt();
            }
        }
        // If the rank is null, then delete the ballot
        else {
            cr.delete();
        }

    }

    public static com.chat.types.community.Community createCommunity(Long userId) {

        log.info("Creating community");
        String name = "new_community_" + UUID.randomUUID().toString().substring(0, 8);

        Tables.Community c = Tables.Community.createIt("name", name,
                "modified_by_user_id", userId);

        CommunityUser.createIt("user_id", userId,
                "community_id", c.getLong("id"),
                "community_role_id", com.chat.types.community.CommunityRole.CREATOR.getVal());

        CommunityView dfv = CommunityView.findFirst("id = ?", c.getLongId());
        List<CommunityUserView> udv = CommunityUserView.where("community_id = ?", c.getLongId());

        return com.chat.types.community.Community.create(dfv, null, udv, null);
    }

    public static com.chat.types.community.Community saveCommunity(Long userId, com.chat.types.community.Community co_) {

        Timestamp cTime = new Timestamp(new Date().getTime());

        Tables.Community c = Tables.Community.findFirst("id = ?" , co_.getId());
        LazyList<CommunityUserView> cuv = CommunityUserView.where("community_id = ?", co_.getId());

        log.info(cuv.toJson(true));
        log.info(co_.json());

        if (co_.getName() != null) c.set("name" , co_.getName());
        if (co_.getText() != null) c.set("text_" , co_.getText());
        if (co_.getPrivate_() != null) c.set("private" , co_.getPrivate_());
        if (co_.getDeleted() != null) c.set("deleted", co_.getDeleted());

        c.set("modified_by_user_id", userId);
        c.set("modified" , cTime);

        try {
            c.saveIt();
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getLocalizedMessage().contains("already exists")) {
                throw new NoSuchElementException("Community already exists");
            }
        }

        // Add the community tags
        if (co_.getTags() != null) {
            CommunityTag.delete("community_id = ?", co_.getId());

            for (Tag tag : co_.getTags()) {
                CommunityTag.createIt("community_id", co_.getId(),
                        "tag_id", tag.getId());
            }
        }

        if (co_.getPrivateUsers() != null) {

            CommunityUser.delete("community_id = ? and community_role_id = ?", co_.getId(), CommunityRole.USER.getVal());

            for (User userObj : co_.getPrivateUsers()) {
                CommunityUser.createIt("community_id", co_.getId(),
                        "user_id", userObj.getId(),
                        "community_role_id", CommunityRole.USER.getVal());
            }

        }

        if (co_.getBlockedUsers() != null) {

            CommunityUser.delete("community_id = ? and community_role_id = ?", co_.getId(), CommunityRole.BLOCKED.getVal());

            for (User userObj : co_.getBlockedUsers()) {
                CommunityUser.createIt("community_id", co_.getId(),
                        "user_id", userObj.getId(),
                        "community_role_id", CommunityRole.BLOCKED.getVal());
            }
        }

        if (co_.getModerators() != null) {

            CommunityUser.delete("community_id = ? and community_role_id = ?", co_.getId(), CommunityRole.MODERATOR.getVal());

            for (User userObj : co_.getModerators()) {
                CommunityUser.createIt("community_id", co_.getId(),
                        "user_id", userObj.getId(),
                        "community_role_id", CommunityRole.MODERATOR.getVal());
            }
        }



        // Fetch the full view
        CommunityView cv = CommunityView.findFirst("id = ?", co_.getId());
        List<CommunityTagView> ctv = CommunityTagView.where("community_id = ?", co_.getId());
        List<CommunityUserView> cuvO = CommunityUserView.where("community_id = ?", co_.getId());

        com.chat.types.community.Community coOut = com.chat.types.community.Community.create(cv, ctv, cuvO, null);

        return coOut;
    }

    public static com.chat.types.community.Community saveFavoriteCommunity(Long userId, Long communityId) {

        CommunityUser cu = CommunityUser.findFirst(
                "user_id = ? and community_id = ?", userId, communityId);

        if (cu == null) {
            CommunityUser.createIt("user_id", userId,
                    "community_id", communityId,
                    "community_role_id", CommunityRole.USER.getVal());

            CommunityNoTextView cntv = CommunityNoTextView.findFirst("id = ?", communityId);

            return com.chat.types.community.Community.create(cntv, null, null, null);
        } else {
            return null;
        }
    }


    public static void deleteFavoriteCommunity(Long userId, Long communityId) {

        CommunityUser cu = CommunityUser.findFirst(
                "user_id = ? and community_id = ?", userId, communityId);

        cu.delete();

    }



    public static String setCookiesForLogin(Tables.User user, String auth, Response res) {
        Boolean secure = DataSources.SSL;

        res.cookie("auth" , auth, DataSources.EXPIRE_SECONDS, secure);
        res.cookie("id" , user.getId().toString(), DataSources.EXPIRE_SECONDS, secure);
        res.cookie("name" , user.getString("name"), DataSources.EXPIRE_SECONDS, secure);

        return "Logged in";
    }

    public static String setCookiesForLogin(FullUser fu, String auth, Response res) {
        Boolean secure = DataSources.SSL;

        res.cookie("auth" , auth, DataSources.EXPIRE_SECONDS, secure);
        res.cookie("id" , fu.getString("user_id"), DataSources.EXPIRE_SECONDS, secure);
        res.cookie("username" , fu.getString("name"), DataSources.EXPIRE_SECONDS, secure);

        return "Logged in";
    }
}
