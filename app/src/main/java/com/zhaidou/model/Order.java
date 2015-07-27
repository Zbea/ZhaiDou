package com.zhaidou.model;

/**
 * Created by wangclark on 15/7/24.
 */
public class Order {
    private long orderId;
    private String time;
    private double price;

    private int state;

    public enum STATE {
        SUCCESS,CANCEL;
    }


    public long orderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", time='" + time + '\'' +
                ", price=" + price +
                ", state=" + state +
                '}';
    }
}
