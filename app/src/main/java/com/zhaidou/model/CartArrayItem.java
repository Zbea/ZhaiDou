package com.zhaidou.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by roy on 15/11/30.
 */
public class CartArrayItem implements Serializable
{
    public int id;
    public String storeId;//供应商 id
    public String storeName;
    public int storeCount;//供应商商品数量
    public double storeMoney;//供应商总钱
    public List<CartGoodsItem> goodsItems;//商品列表

}
