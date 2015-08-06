package com.zhaidou.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by wangclark on 15/7/27.
 */
public class GoodDetail implements Serializable
{
    private int id;
    private String title;
    private String designer;
    private String end_time;
    private int total_count;
    private double price;
    private double cost_price;
    private int discount;
    private List<Specification> specifications;
    private List<GoodInfo> goodsInfo;
    private List<String> imgs;

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

    public String getDesigner() {
        return designer;
    }

    public void setDesigner(String designer) {
        this.designer = designer;
    }

    public int getTotal_count() {
        return total_count;
    }

    public void setTotal_count(int total_count) {
        this.total_count = total_count;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getCost_price() {
        return cost_price;
    }

    public void setCost_price(double cost_price) {
        this.cost_price = cost_price;
    }

    public int getDiscount() {
        return discount;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

    public List<Specification> getSpecifications() {
        return specifications;
    }

    public void setSpecifications(List<Specification> specifications) {
        this.specifications = specifications;
    }

    public List<String> getImgs() {
        return imgs;
    }

    public void setImgs(List<String> imgs) {
        this.imgs = imgs;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public List<GoodInfo> getGoodsInfo()
    {
        return goodsInfo;
    }

    public void setGoodsInfo(List<GoodInfo> goodsInfo)
    {
        this.goodsInfo = goodsInfo;
    }

    public GoodDetail(int id, String title, String designer, int total_count, double price, double cost_price, int discount) {
        this.id = id;
        this.title = title;
        this.designer = designer;
        this.total_count = total_count;
        this.price = price;
        this.cost_price = cost_price;
        this.discount = discount;
    }

    @Override
    public String toString() {
        return "GoodDetail{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", designer='" + designer + '\'' +
                ", total_count=" + total_count +
                ", price=" + price +
                ", cost_price=" + cost_price +
                ", discount=" + discount +
                '}';
    }
}
