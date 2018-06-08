package com.chat.db;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

/**
 * Created by tyler on 5/24/16.
 */
public class Tables {

    @Table("user_")
    public static class User extends Model {}

    @Table("user_setting")
    public static class UserSetting extends Model {}

    @Table("user_audit_view")
    public static class UserAuditView extends Model {}

    @Table("login")
    public static class Login extends Model {}

    @Table("comment")
    public static class Comment extends Model {}

    @Table("comment_tree")
    public static class CommentTree extends Model {}

    @Table("comment_breadcrumbs_view")
    public static class CommentBreadcrumbsView extends Model {}

    @Table("comment_threaded_view")
    public static class CommentThreadedView extends Model {}

    @Table("comment_rank")
    public static class CommentRank extends Model {}

    @Table("discussion")
    public static class Discussion extends Model {}

    @Table("discussion_role")
    public static class DiscussionRole extends Model {}

    @Table("discussion_user")
    public static class DiscussionUser extends Model {}

    @Table("discussion_user_view")
    public static class DiscussionUserView extends Model {}

    @Table("discussion_full_view")
    public static class DiscussionFullView extends Model {}

    @Table("discussion_notext_view")
    public static class DiscussionNoTextView extends Model {}

    @Table("discussion_tag_view")
    public static class DiscussionTagView extends Model {}

    @Table("discussion_rank")
    public static class DiscussionRank extends Model {}

    @Table("favorite_discussion_user")
    public static class FavoriteDiscussionUser extends Model {}

    @Table("favorite_discussion_user_view")
    public static class FavoriteDiscussionUserView extends Model {}

    @Table("discussion_tag")
    public static class DiscussionTag extends Model {}

    @Table("tag")
    public static class Tag extends Model {}

    @Table("ranking_constants")
    public static class RankingConstants extends Model {}

    @Table("censored_word")
    public static class CensoredWord extends Model {}

    @Table("tags_view")
    public static class TagsView extends Model {}

    @Table("community")
    public static class Community extends Model {}

    @Table("community_view")
    public static class CommunityView extends Model {}

    @Table("community_notext_view")
    public static class CommunityNoTextView extends Model {}

    @Table("community_role")
    public static class CommunityRole extends Model {}

    @Table("community_tag")
    public static class CommunityTag extends Model {}

    @Table("community_tag_view")
    public static class CommunityTagView extends Model {}

    @Table("community_rank")
    public static class CommunityRank extends Model {}

    @Table("community_user")
    public static class CommunityUser extends Model {}

    @Table("community_user_view")
    public static class CommunityUserView extends Model {}

    @Table("community_audit_view")
    public static class CommunityAuditView extends Model {}

}
