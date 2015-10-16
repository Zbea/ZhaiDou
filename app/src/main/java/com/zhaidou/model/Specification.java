package com.zhaidou.model;

import java.io.Serializable;

/**
 * Created by wangclark on 15/7/27.
 */
public class Specification implements Serializable
{
    private int id;
    private String title;
    public int num;
    public double price;
    public double oldPrice;
    public boolean isBuy;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getNum()
    {
        return num;
    }

    public void setNum(int num)
    {
        this.num = num;
    }

    public double getPrice()
    {
        return price;
    }

    public void setPrice(double price)
    {
        this.price = price;
    }

    public double getOldPrice()
    {
        return oldPrice;
    }

    public void setOldPrice(double oldPrice)
    {
        this.oldPrice = oldPrice;
    }

    public boolean isBuy()
    {
        return isBuy;
    }

    public void setBuy(boolean isBuy)
    {
        this.isBuy = isBuy;
    }

    public Specification(int id, String title,double price,double oldPrice) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.oldPrice = oldPrice;
    }

    @Override
    public String toString() {
        return "Specification{" +
                "id=" + id +
                ", title='" + title + '\'' +
                '}';
    }
}
