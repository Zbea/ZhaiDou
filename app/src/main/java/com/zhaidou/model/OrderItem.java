package com.zhaidou.model;

/**
 * Created by wangclark on 15/8/5.
 */
public class OrderItem {
    private int id;
    private int price;
    private int count;
    private int cost_price;
    private String merchandise;
    private String specification;
    private int merchandise_id;
    private String merch_img;

    public int getCost_price() {
        return cost_price;
    }

    public void setCost_price(int cost_price) {
        this.cost_price = cost_price;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getMerchandise() {
        return merchandise;
    }

    public void setMerchandise(String merchandise) {
        this.merchandise = merchandise;
    }

    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public int getMerchandise_id() {
        return merchandise_id;
    }

    public void setMerchandise_id(int merchandise_id) {
        this.merchandise_id = merchandise_id;
    }

    public String getMerch_img() {
        return merch_img;
    }

    public void setMerch_img(String merch_img) {
        this.merch_img = merch_img;
    }

    public OrderItem() {
    }

    public OrderItem(int id, int price, int count, int cost_price, String merchandise, String specification, int merchandise_id, String merch_img) {
        this.id = id;
        this.price = price;
        this.count = count;
        this.cost_price = cost_price;
        this.merchandise = merchandise;
        this.specification = specification;
        this.merchandise_id = merchandise_id;
        this.merch_img = merch_img;
    }
}
