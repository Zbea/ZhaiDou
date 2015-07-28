package com.zhaidou.model;

import java.io.Serializable;

/**
 * Created by roy on 15/7/28.
 */
public class CartItem implements Serializable
{

    public int id;
    public String name;
    public String imageUrl;
    public double currentPrice;
    public double formalPrice;
    //省钱数单价
    public double saveMoney;
    //省钱总数
    public double saveTotalMoney;
    //总钱
    public double totalMoney;
    //数量
    public int num;
    //规格
    public String size;
    //规格id
    public int sizeId;
    //是否下架
    public boolean isPublish;

}
