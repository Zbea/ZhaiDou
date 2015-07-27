package com.zhaidou.model;

import java.io.Serializable;

/**
 * Created by roy on 15/7/23.
 */
public class ShopSpecialItem implements Serializable
{
    //id
    int id;
    //名称
    String title;
    String sale;
    String time;
    String imageUrl;

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getImageUrl()
    {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl)
    {
        this.imageUrl = imageUrl;
    }

    public String getTime()
    {
        return time;
    }

    public void setTime(String time)
    {
        this.time = time;
    }

    public String getSale()
    {
        return sale;
    }

    public void setSale(String sale)
    {
        this.sale = sale;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public ShopSpecialItem(int id, String imageUrl, String time, String sale, String title)
    {
        this.id = id;
        this.imageUrl = imageUrl;
        this.time = time;
        this.sale = sale;
        this.title = title;
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
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
