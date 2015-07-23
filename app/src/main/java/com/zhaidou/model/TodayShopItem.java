package com.zhaidou.model;

import java.io.Serializable;

/**
 * Created by roy on 15/7/23.
 */
public class TodayShopItem implements Serializable
{
    //标题
    public String title;
    //评语
    public String introduce;
    //图片地址
    public String imageUrl;
    //当前价格
    public double currentPrice;
    //原价
    public double formerPrice;

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getIntroduce()
    {
        return introduce;
    }

    public void setIntroduce(String introduce)
    {
        this.introduce = introduce;
    }

    public String getImageUrl()
    {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl)
    {
        this.imageUrl = imageUrl;
    }

    public double getCurrentPrice()
    {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice)
    {
        this.currentPrice = currentPrice;
    }

    public double getFormerPrice()
    {
        return formerPrice;
    }

    public void setFormerPrice(double formerPrice)
    {
        this.formerPrice = formerPrice;
    }

    @Override
    public String toString()
    {
        return "TodayShopItem{" +
                "title='" + title + '\'' +
                ", introduce='" + introduce + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", currentPrice=" + currentPrice +
                ", formerPrice=" + formerPrice +
                '}';
    }

    public TodayShopItem(String title, String introduce, String imageUrl, double currentPrice, double formerPrice)
    {
        this.title = title;
        this.introduce = introduce;
        this.imageUrl = imageUrl;
        this.currentPrice = currentPrice;
        this.formerPrice = formerPrice;
    }

//    public TodayShopItem()
//    {
//    }
}
