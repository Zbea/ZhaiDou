package com.zhaidou.model;

import java.io.Serializable;

/**
 * Created by roy on 15/7/23.
 */
public class ShopSpecialItem implements Serializable
{
    //id
    public int id;
    //名称
    public String title;
    public String sale;
    public String time;
    public String startTime;
    public String endTime;
    //剩余时间
    public String overTime;
    public String imageUrl;
    public String isNew;
    public ShopSpecialItem(int id, String title, String sale, String time, String startTime, String endTime, String overTime, String imageUrl,String isNew)
    {
        this.id = id;
        this.title = title;
        this.sale = sale;
        this.time = time;
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
                ", time='" + time + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", overTime='" + overTime + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
