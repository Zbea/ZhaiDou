package com.zhaidou.model;

/**
 * Created by wangclark on 15/8/5.
 */
public class OrderItem {
    private int id;
    private double price;
    private int count;
    private double cost_price;
    private String merchandise;
    private String specification;
    private int merchandise_id;
    private String merch_img;
    private int sale_cate;

    public double getCost_price() {
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

    public double getPrice() {
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

    public int getSale_cate() {
        return sale_cate;
    }

    public void setSale_cate(int sale_cate) {
        this.sale_cate = sale_cate;
    }

    public OrderItem() {
    }

    public OrderItem(int id, double price, int count, double cost_price, String merchandise, String specification, int merchandise_id, String merch_img) {
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
