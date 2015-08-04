package com.zhaidou.model;

import java.io.Serializable;

/**
 * Created by roy on 15/7/28.
 */
public class CartItem implements Serializable
{
    //商品id
    public int id;
    //名称
    public String name;
    //图片地址
    public String imageUrl;
    //现价
    public double currentPrice;
    //原价
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
    public String isPublish;
    //是否卖光
    public String isOver;
    //是否选中
    public boolean isCheck;
    //生成时间
    public long creatTime;

    public CartItem(int id, long creatTime, String name, String imageUrl, double currentPrice,
                    double formalPrice,double saveMoney, double saveTotalMoney, double totalMoney,
                    int num, String size, int sizeId, String isPublish, String isOver, boolean isCheck)
    {
        this.id = id;
        this.creatTime = creatTime;
        this.name = name;
        this.imageUrl = imageUrl;
        this.currentPrice = currentPrice;
        this.formalPrice = formalPrice;
        this.saveMoney = saveMoney;
        this.saveTotalMoney = saveTotalMoney;
        this.totalMoney = totalMoney;
        this.num = num;
        this.size = size;
        this.sizeId = sizeId;
        this.isPublish = isPublish;
        this.isOver = isOver;
        this.isCheck = isCheck;
    }

    public CartItem()
    {
    }
}
