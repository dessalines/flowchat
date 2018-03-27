package com.chat.db;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import ch.qos.logback.classic.Logger;
import com.chat.DataSources;
import com.chat.db.Tables.*;
import com.chat.tools.Tools;
import com.chat.types.community.Community;
import com.chat.types.community.CommunityRole;
import com.chat.types.discussion.Discussion;
import com.chat.types.discussion.DiscussionRole;
import com.chat.types.tag.Tag;
import com.chat.types.user.SortType;
import com.chat.types.user.User;
import com.chat.types.user.ViewType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.javalite.activejdbc.LazyList;

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
        Comment c = Comment.createIt("discussion_id", discussionId,
                "text_", text,
                "user_id", userId,
                "modified_by_user_id", userId);


        Long childId = c.getLong("id");

        // This is necessary, because of the 0 path length to itself one
        pbs.add(childId);

        Collections.reverse(pbs);


        // Create the comment_tree
        for (int i = 0; i < pbs.size(); i++) {

            Long parentId = pbs.get(i);

            // i is the path length
            CommentTree.createIt("parent_id", parentId,
                    "child_id", childId,
                    "path_length", i);
        }

        return c;

    }

    public static Comment editComment(Long userId, Long commentId, String text) {

        // Find the comment
        Comment c = Comment.findFirst("id = ?", commentId);

        Timestamp cTime = new Timestamp(new Date().getTime());

        // Create with add modified date
        c.set("text_", text,
                "modified", cTime,
                "modified_by_user_id", userId).saveIt();

        return c;

    }

    public static Comment deleteComment(Long userId, Long commentId) {
        // Find the comment
        Comment c = Comment.findFirst("id = ?", commentId);

        Timestamp cTime = new Timestamp(new Date().getTime());

        // Create with add modified date
        c.set("deleted", true,
                "modified", cTime,
                "modified_by_user_id", userId).saveIt();

        return c;
    }

    public static User getOrCreateUserObj(Long id, String auth) {

        log.debug("getOrCreateUser id = " + id + " auth = " + auth);

        User userObj;
        Tables.UserView dbUser;

        if (id != null) {
            if (auth == null || auth.equals("null")) {
                dbUser = Tables.UserView.findFirst("id = ?", id);
            } else {
                dbUser = Tables.UserView.findFirst("auth = ?", auth);
            }
        } else {
            Tables.User newUser = Actions.createUser();
            dbUser = Tables.UserView.findFirst("id = ?", newUser.getLongId());
        }

        userObj = User.create(dbUser);

        return userObj;
    }

    public static User getOrCreateUserObj(Request req) {

        log.debug(req.headers("user"));

        UserFromHeader ufh;

        if (req.headers("user") != null) {
            ufh = UserFromHeader.fromJson(req.headers("user"));
        } else {
            ufh = new UserFromHeader(null, null);
        }

        return getOrCreateUserObj(ufh.getId(), ufh.getAuth());

    }

    public static User updateUser(User userIn) {
        UserSetting us = UserSetting.findFirst("user_id = ?", userIn.getId());

        if (userIn.getSettings().getReadOnboardAlert() != null) {
            us.setBoolean("read_onboard_alert", userIn.getSettings().getReadOnboardAlert());
        }

        if (userIn.getSettings().getDefaultViewTypeRadioValue() != null) {
            us.setInteger("default_view_type_id", ViewType.getFromRadioValue(userIn.getSettings().getDefaultViewTypeRadioValue()).getVal());
        }

        if (userIn.getSettings().getDefaultSortTypeRadioValue() != null) {
            us.setInteger("default_sort_type_id", SortType.getFromRadioValue(userIn.getSettings().getDefaultSortTypeRadioValue()).getVal());
        }

        us.saveIt();

        UserView dbUser = Tables.UserView.findFirst("id = ?", userIn.getId());
        User userObj = User.create(dbUser);

        return userObj;
    }

    public static Discussion createDiscussion(Long userId) {

        log.debug("Creating discussion");
        String title = "A new discussion";

        Tables.Discussion d = Tables.Discussion.createIt("title", title,
                "modified_by_user_id", userId);

        DiscussionUser.createIt("user_id", userId,
                "discussion_id", d.getLong("id"),
                "discussion_role_id", DiscussionRole.CREATOR.getVal());

        FavoriteDiscussionUser.createIt("user_id", userId,
                "discussion_id", d.getLong("id"));

        DiscussionFullView dfv = DiscussionFullView.findFirst("id = ?", d.getLongId());
        List<DiscussionUserView> udv = DiscussionUserView.where("discussion_id = ?", d.getLongId());
        CommunityNoTextView cntv = CommunityNoTextView.findFirst("id = ?", dfv.getLong("community_id"));

        return Discussion.create(dfv, cntv, null, udv, null, null);
    }

    public static Discussion saveDiscussion(Long userId, Discussion do_) {

        Timestamp cTime = new Timestamp(new Date().getTime());

        Tables.Discussion d = Tables.Discussion.findFirst("id = ?", do_.getId());
        LazyList<DiscussionUserView> udv = DiscussionUserView.where("discussion_id = ?", do_.getId());

        log.debug(udv.toJson(true));
        log.debug(do_.json());

        if (do_.getTitle() != null) d.set("title", do_.getTitle());
        if (do_.getLink() != null) d.set("link", do_.getLink());
        if (do_.getText() != null) d.set("text_", do_.getText());
        if (do_.getPrivate_() != null) d.set("private", do_.getPrivate_());
        if (do_.getDeleted() != null) d.set("deleted", do_.getDeleted());
        if (do_.getCommunity() != null) d.set("community_id", do_.getCommunity().getId());

        d.set("modified_by_user_id", userId);
        d.set("modified", cTime);
        d.saveIt();

        // Add the discussion tags
        if (do_.getTags() != null) {
            diffCreateOrDeleteDiscussionTags(do_);
        }

        if (do_.getPrivateUsers() != null) {
            diffCreateOrDeleteDiscussionUsers(do_.getId(), do_.getPrivateUsers(), DiscussionRole.USER);
        }

        if (do_.getBlockedUsers() != null) {
            diffCreateOrDeleteDiscussionUsers(do_.getId(), do_.getBlockedUsers(), DiscussionRole.BLOCKED);
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

    private static void diffCreateOrDeleteDiscussionUsers(Long discussionId, List<User> users, DiscussionRole role) {
        Set<Long> postUserIds = users.stream()
                .map(user -> user.getId())
                .collect(Collectors.toSet());

        Set<Long> dbUserIds = DiscussionUser.where("discussion_id = ? and discussion_role_id = ?",
                discussionId, role.getVal()).collectDistinct("user_id");

        Set<Long> diffPostUserIds = new LinkedHashSet<>(postUserIds);
        Set<Long> diffDbTagIds = new LinkedHashSet<>(dbUserIds);

        diffPostUserIds.removeAll(dbUserIds);
        diffDbTagIds.removeAll(postUserIds);

        // Delete everything in the DB, that's not posted.
        if (!diffDbTagIds.isEmpty()) {
            DiscussionUser.delete("discussion_id = ? and discussion_role_id = ? and user_id in " +
                            Tools.convertListToInQuery(diffDbTagIds),
                    discussionId,
                    role.getVal());
        }

        for (Long uId : diffPostUserIds) {
            DiscussionUser.createIt("discussion_id", discussionId,
                    "user_id", uId,
                    "discussion_role_id", role.getVal());
        }
    }

    private static void diffCreateOrDeleteDiscussionTags(Discussion do_) {
        Set<Long> postTagIds = do_.getTags().stream()
                .map(tag -> tag.getId())
                .collect(Collectors.toSet());

        // Fetch the existing community tags from the DB
        Set<Long> dbTagIds = DiscussionTag.where("discussion_id = ?", do_.getId()).collectDistinct("tag_id");

        Set<Long> diffPostTagIds = new LinkedHashSet<>(postTagIds);
        Set<Long> diffDbTagIds = new LinkedHashSet<>(dbTagIds);

        diffPostTagIds.removeAll(dbTagIds);
        diffDbTagIds.removeAll(postTagIds);

        // Delete everything in the DB, that's not posted.
        if (!diffDbTagIds.isEmpty()) {
            DiscussionTag.delete("discussion_id = ? and tag_id in " + Tools.convertListToInQuery(diffDbTagIds), do_.getId());
        }

        // Add everything posted, thats not in the db
        for (Long tagId : diffPostTagIds) {
            DiscussionTag.createIt("discussion_id", do_.getId(),
                    "tag_id", tagId);
        }
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


    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class UserFromHeader {
        private Long id;
        private String auth;

        public UserFromHeader() {}

        public UserFromHeader(Long id, String auth) {
            this.id = id;
            this.auth = auth;
        }

        public static UserFromHeader fromJson(String dataStr) {
            try {
                log.debug(dataStr);
                return Tools.JACKSON.readValue(dataStr, UserFromHeader.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        public Long getId() {
            return id;
        }

        public String getAuth() {
            return auth;
        }

    }

    public static Tables.User createUser() {
        Tables.User user = Tables.User.createIt(
                "name", Tools.generateSecureRandom());
        user.set("name", "user_" + user.getLongId()).saveIt();

        UserSetting.createIt("user_id", user.getLongId());

        return user;
    }

    public static User login(String userOrEmail, String password) {

        // Find the user, then create a login for them

        UserView uv = UserView.findFirst("name = ? or email = ?", userOrEmail, userOrEmail);

        Login login;
        String auth;

        if (uv == null) {
            throw new NoSuchElementException("Incorrect user/email");
        } else {
            FullUser fu = FullUser.findFirst("user_id = ?", uv.getLongId());

            String encryptedPassword = fu.getString("password_encrypted");

            Boolean correctPass = Tools.PASS_ENCRYPT.checkPassword(password, encryptedPassword);

            if (correctPass) {

                auth = Tools.generateSecureRandom();
                login = Login.createIt("user_id", fu.getInteger("user_id"),
                        "auth", auth,
                        "expire_time", Tools.newExpireTimestamp());

            } else {
                throw new NoSuchElementException("Incorrect Password");
            }
        }

        UserView dbUser = UserView.findFirst("login_id = ?", login.getLongId());

        User userObj = User.create(dbUser);
        userObj.setAuth(auth);

        return userObj;

    }

    public static User signup(String userName, String password, String verifyPassword, String email) {

        if (email != null && email.equals("")) {
            email = null;
        }

        if (!password.equals(verifyPassword)) {
            throw new NoSuchElementException("Passwords are different");
        }

        // Find the user, then create a login for them
        UserView uv;
        if (email != null) {
            uv = UserView.findFirst("name = ? or email = ?", userName, email);
        } else {
            uv = UserView.findFirst("name = ?", userName);
        }

        Login login;
        String auth;
        if (uv == null) {

            // Create the user and full user
            Tables.User user = Tables.User.createIt(
                    "name", userName);

            log.debug("encrypting the user password");
            String encryptedPassword = Tools.PASS_ENCRYPT.encryptPassword(password);

            FullUser fu = FullUser.createIt("user_id", user.getId(),
                    "email", email,
                    "password_encrypted", encryptedPassword);

            // now login that user
            auth = Tools.generateSecureRandom();
            login = Login.createIt("user_id", user.getId(),
                    "auth", auth,
                    "expire_time", Tools.newExpireTimestamp());

            UserSetting.createIt("user_id", user.getLongId());

        } else {
            throw new NoSuchElementException("Username/email already exists");
        }

        UserView dbUser = UserView.findFirst("login_id = ?", login.getLongId());

        User userObj = User.create(dbUser);
        userObj.setAuth(auth);

        return userObj;

    }

    public static String saveCommentVote(Long userId, Long commentId, Integer rank) {

        String message = null;
        // fetch the vote if it exists
        CommentRank c = CommentRank.findFirst("user_id = ? and comment_id = ?",
                userId, commentId);


        if (c == null) {
            if (rank != null) {
                CommentRank.createIt(
                        "comment_id", commentId,
                        "user_id", userId,
                        "rank", rank);
                message = "Comment Vote Created";
            } else {
                message = "Comment Vote not created";
            }
        } else {
            if (rank != null) {
                c.set("rank", rank).saveIt();
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
        DiscussionRank d = DiscussionRank.findFirst("user_id = ? and discussion_id = ?",
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
        CommunityRank cr = CommunityRank.findFirst("user_id = ? and community_id = ?",
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

    public static Community createCommunity(Long userId) {

        log.debug("Creating community");
        String name = "new_community_" + UUID.randomUUID().toString().substring(0, 8);

        Tables.Community c = Tables.Community.createIt("name", name,
                "modified_by_user_id", userId);

        CommunityUser.createIt("user_id", userId,
                "community_id", c.getLong("id"),
                "community_role_id", CommunityRole.CREATOR.getVal());

        CommunityView dfv = CommunityView.findFirst("id = ?", c.getLongId());
        List<CommunityUserView> udv = CommunityUserView.where("community_id = ?", c.getLongId());

        return Community.create(dfv, null, udv, null);
    }

    public static Community saveCommunity(Long userId, Community co_) {

        Timestamp cTime = new Timestamp(new Date().getTime());

        Tables.Community c = Tables.Community.findFirst("id = ?", co_.getId());
        LazyList<CommunityUserView> cuv = CommunityUserView.where("community_id = ?", co_.getId());

        log.debug(cuv.toJson(true));
        log.debug(co_.json());

        if (co_.getName() != null) c.set("name", co_.getName());
        if (co_.getText() != null) c.set("text_", co_.getText());
        if (co_.getPrivate_() != null) c.set("private", co_.getPrivate_());
        if (co_.getDeleted() != null) c.set("deleted", co_.getDeleted());

        c.set("modified_by_user_id", userId);
        c.set("modified", cTime);

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
            diffCreateOrDeleteCommunityTags(co_);
        }

        if (co_.getPrivateUsers() != null) {
            diffCreateOrDeleteCommunityUsers(co_.getId(), co_.getPrivateUsers(), CommunityRole.USER);
        }

        if (co_.getBlockedUsers() != null) {
            diffCreateOrDeleteCommunityUsers(co_.getId(), co_.getBlockedUsers(), CommunityRole.BLOCKED);
        }

        if (co_.getModerators() != null) {
            diffCreateOrDeleteCommunityUsers(co_.getId(), co_.getModerators(), CommunityRole.MODERATOR);
        }


        // Fetch the full view
        CommunityView cv = CommunityView.findFirst("id = ?", co_.getId());
        List<CommunityTagView> ctv = CommunityTagView.where("community_id = ?", co_.getId());
        List<CommunityUserView> cuvO = CommunityUserView.where("community_id = ?", co_.getId());

        Community coOut = Community.create(cv, ctv, cuvO, null);

        return coOut;
    }

    private static void diffCreateOrDeleteCommunityUsers(Long communityId, List<User> users, CommunityRole role) {
        Set<Long> postUserIds = users.stream()
                .map(user -> user.getId())
                .collect(Collectors.toSet());

        Set<Long> dbUserIds = CommunityUser.where("community_id = ? and community_role_id = ?",
                communityId, role.getVal()).collectDistinct("user_id");

        Set<Long> diffPostUserIds = new LinkedHashSet<>(postUserIds);
        Set<Long> diffDbTagIds = new LinkedHashSet<>(dbUserIds);

        diffPostUserIds.removeAll(dbUserIds);
        diffDbTagIds.removeAll(postUserIds);

        // Delete everything in the DB, that's not posted.
        if (!diffDbTagIds.isEmpty()) {
            CommunityUser.delete("community_id = ? and community_role_id = ? and user_id in " +
                    Tools.convertListToInQuery(diffDbTagIds),
                    communityId,
                    role.getVal());
        }

        for (Long uId : diffPostUserIds) {
            CommunityUser.createIt("community_id", communityId,
                    "user_id", uId,
                    "community_role_id", role.getVal());
        }
    }

    private static void diffCreateOrDeleteCommunityTags(Community co_) {
        Set<Long> postTagIds = co_.getTags().stream()
                .map(tag -> tag.getId())
                .collect(Collectors.toSet());

        // Fetch the existing community tags from the DB
        Set<Long> dbTagIds = CommunityTag.where("community_id = ?", co_.getId()).collectDistinct("tag_id");

        Set<Long> diffPostTagIds = new LinkedHashSet<>(postTagIds);
        Set<Long> diffDbTagIds = new LinkedHashSet<>(dbTagIds);

        diffPostTagIds.removeAll(dbTagIds);
        diffDbTagIds.removeAll(postTagIds);

        // Delete everything in the DB, that's not posted.
        if (!diffDbTagIds.isEmpty()) {
            CommunityTag.delete("community_id = ? and tag_id in " + Tools.convertListToInQuery(diffDbTagIds), co_.getId());
        }

        // Add everything posted, thats not in the db
        for (Long tagId : diffPostTagIds) {
            CommunityTag.createIt("community_id", co_.getId(),
                    "tag_id", tagId);
        }
    }

    public static Community saveFavoriteCommunity(Long userId, Long communityId) {

        CommunityUser cu = CommunityUser.findFirst(
                "user_id = ? and community_id = ?", userId, communityId);

        if (cu == null) {
            CommunityUser.createIt("user_id", userId,
                    "community_id", communityId,
                    "community_role_id", CommunityRole.USER.getVal());

            CommunityNoTextView cntv = CommunityNoTextView.findFirst("id = ?", communityId);

            return Community.create(cntv, null, null, null);
        } else {
            return null;
        }
    }


    public static void deleteFavoriteCommunity(Long userId, Long communityId) {

        CommunityUser cu = CommunityUser.findFirst(
                "user_id = ? and community_id = ?", userId, communityId);

        cu.delete();

    }


    public static Tables.Tag getOrCreateTagFromSubreddit(String subredditName) {

        Long userId = 4L; // cardinal

        // Get the tag / or create it if it doesn't exist
        Tables.Tag t = Tables.Tag.findFirst("name = ?", subredditName);

        if (t == null) {
            t = Tables.Tag.createIt("name", subredditName);

        }

        return t;
    }

    public static Tables.Discussion getOrCreateDiscussionFromRedditPost(
            Long tagId, String title, String link, String selfText, Date created) {

        Long userId = 4L; // cardinal
        Long communityId = 1L; // Vanilla

        Tables.Discussion d = Tables.Discussion.findFirst("title = ?", title);

        if (d == null) {
            d = Tables.Discussion.createIt("title", title,
                    "community_id", communityId,
                    "modified_by_user_id", userId,
                    "created", new Timestamp(created.getTime()));

            if (!link.isEmpty()) d.set("link", link);
            if (!selfText.isEmpty()) d.set("text_", selfText);

            d.saveIt();

            DiscussionUser.createIt("user_id", userId,
                    "discussion_id", d.getLong("id"),
                    "discussion_role_id", DiscussionRole.CREATOR.getVal());

            DiscussionTag.createIt("discussion_id", d.getLong("id"),
                    "tag_id", tagId);
        }

        return d;

    }

}
