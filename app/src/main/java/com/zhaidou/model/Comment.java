package com.zhaidou.model;

import java.util.ArrayList;
import java.util.List;

public class Comment
{
    /**
     * 评论人
     */
    public int userId;
    /**
     * 评论人
     */
    public String header;
    /**
     * 评论人
     */
    public User user;
    /**
     * 回复的人
     */
    public User userReply;
    /**
     * 回复时间
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
     * 回复的评论信息
     */
    public String commentReply;
    /**
     * 回复的评论图片
     */
    public List<String> imagesReply=new ArrayList<String>();

}
