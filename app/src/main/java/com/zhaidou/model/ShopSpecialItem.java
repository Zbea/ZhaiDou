package com.zhaidou.model;

import java.io.Serializable;

/**
 * Created by roy on 15/7/23.
 */
public class ShopSpecialItem implements Serializable
{
    //id
    public int id;
    public String goodsId;
    //名称
    public String title;
    public String sale;
    public long startTime;
    public long endTime;
    //剩余时间
    public int overTime;
    public String imageUrl;
    public int isNew;
    public ShopSpecialItem(String id, String title, String sale, long startTime, long endTime, int overTime, String imageUrl,int isNew)
    {
        goodsId= id;
        this.title = title;
        this.sale = sale;
        this.startTime = startTime;
        this.endTime = endTime;
        this.overTime = overTime;
        this.imageUrl = imageUrl;
        this.isNew = isNew;
    }

    public ShopSpecialItem()
    {
    }

    @Override
    public String toString()
    {
        return "ShopSpecialItem{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", sale='" + sale + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", overTime='" + overTime + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
