package com.zhaidou.model;

import java.io.Serializable;

/**
 * Created by roy on 15/7/28.
 */
public class CartGoodsItem implements Serializable
{
    //用户id
    public String userId;//*
    //用户id
    public String storeId;//*
    //商品id
    public int id;
    //名称
    public String goodsId;
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
    //添加数量
    public int num;
    //商品库存
    public int count;
    //规格Sku
    public String sizeId;//*
    //规格
    public String size;
    //是否下架
    public String isPublish;
    //是否卖光
    public String isOver;
    //是否选中
    public boolean isCheck;
    //是否零元特卖
    public String isOSale;
    //是否过期
    public String isDate;
    //生成时间
    public long creatTime;

    public CartGoodsItem()
    {
    }
}
