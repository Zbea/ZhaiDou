package com.zhaidou.model;

import java.io.Serializable;

/**
 * Created by roy on 15/7/23.
 */
public class ShopTodayItem implements Serializable
{
    public int id;
    //标题
    public String title;
    //评语
    public String designer;
    //图片地址
    public String imageUrl;
    //当前价格
    public double currentPrice;
    //原价
    public double formerPrice;

    public int num;



    public ShopTodayItem(int id,String title, String designer, String imageUrl, double currentPrice, double formerPrice,int num)
    {
        this.id=id;
        this.title = title;
        this.designer = designer;
        this.imageUrl = imageUrl;
        this.currentPrice = currentPrice;
        this.formerPrice = formerPrice;
        this.num=num;
    }

    public ShopTodayItem()
    {
    }
}
