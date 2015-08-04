package com.zhaidou.model;

/**
 * Created by wangclark on 15/7/24.
 */
public class Order {
    private long orderId;
    private String number;
    private int amount;
    private String status;
    private String status_ch;
    private String created_at_for;
    private String created_at;
    private String time;
    private double price;
    private String img;
    private long over_at;


    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus_ch() {
        return status_ch;
    }

    public void setStatus_ch(String status_ch) {
        this.status_ch = status_ch;
    }

    public String getCreated_at_for() {
        return created_at_for;
    }

    public void setCreated_at_for(String created_at_for) {
        this.created_at_for = created_at_for;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
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

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public long getOver_at() {
        return over_at;
    }

    public void setOver_at(long over_at) {
        this.over_at = over_at;
    }

    public Order(long orderId, String number, int amount, String status, String status_ch, String created_at_for, String created_at, String time, double price) {
        this.orderId = orderId;
        this.number = number;
        this.amount = amount;
        this.status = status;
        this.status_ch = status_ch;
        this.created_at_for = created_at_for;
        this.created_at = created_at;
        this.time = time;
        this.price = price;
    }

    public Order() {
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", number='" + number + '\'' +
                ", amount=" + amount +
                ", status='" + status + '\'' +
                ", status_ch='" + status_ch + '\'' +
                ", created_at_for='" + created_at_for + '\'' +
                ", created_at='" + created_at + '\'' +
                ", time='" + time + '\'' +
                ", price=" + price +
                '}';
    }
}
