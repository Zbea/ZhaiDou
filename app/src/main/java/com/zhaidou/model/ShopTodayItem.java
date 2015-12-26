package com.zhaidou.model;

import java.io.Serializable;

/**
 * Created by roy on 15/7/23.
 */
public class ShopTodayItem implements Serializable
{
    public int id;
    public String goodsId;
    //标题
    public String title;
    //评论
    public String comment;
    //图片地址
    public String imageUrl;
    //当前价格
    public double currentPrice;
    //原价
    public double formerPrice;
    //剩余量
    public int num;
    //总量
    public int totalCount;
    //已购量
    public int percentum;


    public ShopTodayItem(String id,String title, String imageUrl, double currentPrice, double formerPrice,int num,int totalCount)
    {
        goodsId=id;
        this.title = title;
        this.imageUrl = imageUrl;
        this.currentPrice = currentPrice;
        this.formerPrice = formerPrice;
        this.num=num;
        this.totalCount = totalCount;
    }

    public ShopTodayItem()
    {
    }
}
