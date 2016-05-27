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
