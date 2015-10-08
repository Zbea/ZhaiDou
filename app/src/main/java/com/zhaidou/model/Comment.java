package com.zhaidou.model;
/**
 * User: Scoield(553899626@qq.com)
 * Date: 2015-09-28
 * Time: 15:34
 * Description:评论的javaBean,
 */
public class Comment {
    private User user;
    private String comment;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Comment(User user, String comment) {
        this.user = user;
        this.comment = comment;
    }

    public Comment() {
    }

    @Override
    public String toString() {
        return "Comment{" +
                "user=" + user +
                ", comment='" + comment + '\'' +
                '}';
    }
}
