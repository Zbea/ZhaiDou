package com.zhaidou.model;

/**
 * Created by wangclark on 15/7/30.
 */
public class Area extends Address {
    private int price;

    public double getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Area{" +
                "price=" + price +
                "} " + super.toString();
    }
}
