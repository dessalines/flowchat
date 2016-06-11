package com.chat.db;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

/**
 * Created by tyler on 5/24/16.
 */
public class Tables {

    @Table("user_")
    public static class User extends Model {}
    public static final User USER = new User();

    @Table("full_user")
    public static class FullUser extends Model {}
    public static final FullUser FULL_USER = new FullUser();

    @Table("user_view")
    public static class UserView extends Model {}
    public static final UserView USER_VIEW = new UserView();

    @Table("login")
    public static class Login extends Model {}
    public static final Login LOGIN = new Login();

    @Table("user_login_view")
    public static class UserLoginView extends Model {}
    public static final UserLoginView USER_LOGIN_VIEW = new UserLoginView();

    @Table("comment")
    public static class Comment extends Model {}
    public static final Comment COMMENT = new Comment();

    @Table("comment_tree")
    public static class CommentTree extends Model {}
    public static final CommentTree COMMENT_TREE = new CommentTree();

    @Table("comment_breadcrumbs_view")
    public static class CommentBreadcrumbsView extends Model {}
    public static final CommentBreadcrumbsView COMMENT_BREADCRUMBS_VIEW = new CommentBreadcrumbsView();

    @Table("comment_threaded_view")
    public static class CommentThreadedView extends Model {}
    public static final CommentThreadedView COMMENT_THREADED_VIEW = new CommentThreadedView();


}
