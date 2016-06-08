package com.zhaidou.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Comment implements Serializable
{
    /**
     * id
     */
    public int id;
    /**
     * 评论人
     */
    public int userId;

    /**
     * 评论人
     */
    public String userName;
    /**
     * 评论人
     */
    public String userImage;
    /**
     * 文章id
     */
    public String articleId;
    /**
     * 文章标题
     */
    public String articleTitle;
    /**
     * 类型
     */
    public String type;
    /**
     * 状态
     */
    public String status;
    /**
     * 时间
     */
    public String time;
    /**
     * 评论信息
     */
    public String comment;
    /**
     * 评论图片
     */
    public List<String> images=new ArrayList<String>();


    /**
     * 回复id
     */
    public int idReply;
    /**
     * 评论人
     */
    public int userIdReply;

    /**
     * 评论人
     */
    public String userNameReply;
    /**
     * 评论人
     */
    public String userImageReply;
    /**
     * 回复的类型
     */
    public String typeReply;
    /**
     * 回复的状态
     */
    public String statusReply;
    /**
     * 回复的时间
     */
    public String timeReply;
    /**
     * 回复的评论信息
     */
    public String commentReply;
    /**
     * 回复的评论图片
     */
    public List<String> imagesReply=new ArrayList<String>();

}
