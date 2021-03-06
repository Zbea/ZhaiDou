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
     * 是否是设计师
     */
    public int isDesigner;
    /**
     * 评论图片
     */
    public List<String> images=new ArrayList<String>();


    /**
     * 回复id
     */
    public int idFormer;
    /**
     * 评论人
     */
    public int userIdFormer;

    /**
     * 评论人
     */
    public String userNameFormer;
    /**
     * 评论人
     */
    public String userImageFormer;
    /**
     * 回复的类型
     */
    public String typeFormer;
    /**
     * 回复的状态
     */
    public String statusFormer;
    /**
     * 回复的时间
     */
    public String timeFormer;
    /**
     * 回复的是否是设计师
     */
    public int isDesignerFormer;
    /**
     * 回复的评论信息
     */
    public String commentFormer;
    /**
     * 回复的评论图片
     */
    public List<String> imagesFormer =new ArrayList<String>();

    /**
     * id : 13
     * content : 111111111
     * imgMd5 : http://timgs.zhaidou.com/comment/20160530/64c3fa54fa9690f84efc90cf7cc427e8.jpg,http://timgs.zhaidou.com/comment/20160530/47b273bc46a1c4132a172a27cfc00e52.jpg
     * commentUserId : 28822
     * commentUserName : 123
     * articleId : 123
     * articleTitle : 123
     * commentType : C
     * status : N
     * createTime : 2016-05-30 20:21:17
     */

    public String content;
    public String imgMd5;
    public int commentUserId;
    public String commentUserName;
    public String commentUserImg;
    public String commentType;
    public String createTime;


}

