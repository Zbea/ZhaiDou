package com.zhaidou.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangclark on 15/7/27.
 */
public class Specification implements Serializable
{
    public int id;
    public String sizeId;
    public String title;
    public String title1;
    public int num;
    public double price;
    public double oldPrice;
    public boolean isBuy;
    public List<String> images=new ArrayList<String>();
    public List<Specification> sizess=new ArrayList<Specification>();
}
