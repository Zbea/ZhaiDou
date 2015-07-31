package com.zhaidou.model;

import java.util.List;

/**
 * Created by wangclark on 15/7/30.
 * 省市区
 */
public class PCityArea {
    private int id;
    private String name;
    private List<PCityArea> datas;
    private int price;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PCityArea> getDatas() {
        return datas;
    }

    public void setDatas(List<PCityArea> datas) {
        this.datas = datas;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
