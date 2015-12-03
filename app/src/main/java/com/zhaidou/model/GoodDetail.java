package com.zhaidou.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangclark on 15/7/27.
 */
public class GoodDetail implements Serializable
{
    public int id;
    public String goodsId;//商品 id
    public String title;
    public String designer;
    public String imageUrl;
    public long end_time;
    public int total_count;
    public double price;
    public double cost_price;
    public String discount;
    public String specificationName;//规格
    public String modelName;//型号
    public List<Specification> specifications;
    public ArrayList<GoodInfo> goodsInfo;
    public ArrayList<String> imgs;


}
