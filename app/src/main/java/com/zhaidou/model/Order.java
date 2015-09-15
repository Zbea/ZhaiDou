package com.zhaidou.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by wangclark on 15/7/24.
 */
public class Order implements Serializable{
    private long orderId;
    private String number;
    private double amount;
    private String status;
    private String status_ch;
    private String created_at_for;
    private String created_at;
    private String time;
    private double price;
    private String img;
    private long over_at;
    private Receiver receiver;
    List<OrderItem> orderItems;
    private String receiver_address;
    private String receiver_phone;
    private String deliver_number;
    private String receiver_name;
    private String node;
    private boolean zero;
    private String parent_name;
    private String city_name;
    private String provider_name;
    public String logisticsNum;

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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
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

    public Receiver getReceiver() {
        return receiver;
    }

    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public String getReceiver_address() {
        return receiver_address;
    }

    public void setReceiver_address(String receiver_address) {
        this.receiver_address = receiver_address;
    }

    public boolean isZero() {
        return zero;
    }

    public void setZero(boolean zero) {
        this.zero = zero;
    }

    public String getReceiver_phone() {
        return receiver_phone;
    }

    public void setReceiver_phone(String receiver_phone) {
        this.receiver_phone = receiver_phone;
    }

    public String getDeliver_number() {
        return deliver_number;
    }

    public void setDeliver_number(String deliver_number) {
        this.deliver_number = deliver_number;
    }

    public String getReceiver_name() {
        return receiver_name;
    }

    public void setReceiver_name(String receiver_name) {
        this.receiver_name = receiver_name;
    }

    public String getNode()
    {
        return node;
    }

    public void setNode(String node)
    {
        this.node = node;
    }

    public String getParent_name() {
        return parent_name;
    }

    public void setParent_name(String parent_name) {
        this.parent_name = parent_name;
    }

    public String getCity_name() {
        return city_name;
    }

    public void setCity_name(String city_name) {
        this.city_name = city_name;
    }

    public String getProvider_name() {
        return provider_name;
    }

    public void setProvider_name(String provider_name) {
        this.provider_name = provider_name;
    }

    public Order(long orderId, String number, double amount, String status, String status_ch, String created_at_for, String created_at, String time, double price) {
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
                ", img='" + img + '\'' +
                ", over_at=" + over_at +
                ", receiver=" + receiver +
                ", orderItems=" + orderItems +
                ", receiver_address='" + receiver_address + '\'' +
                ", receiver_phone='" + receiver_phone + '\'' +
                ", deliver_number='" + deliver_number + '\'' +
                ", receiver_name='" + receiver_name + '\'' +
                ", node='" + node + '\'' +
                '}';
    }

    public Order(String time, long orderId, String number, double amount, String status, String status_ch, String created_at_for, String created_at, Receiver receiver, List<OrderItem> orderItems, String receiver_address, String receiver_phone, String deliver_number, String receiver_name) {
        this.time = time;
        this.orderId = orderId;
        this.number = number;
        this.amount = amount;
        this.status = status;
        this.status_ch = status_ch;
        this.created_at_for = created_at_for;
        this.created_at = created_at;
        this.receiver = receiver;
        this.orderItems = orderItems;
        this.receiver_address = receiver_address;
        this.receiver_phone = receiver_phone;
        this.deliver_number = deliver_number;
        this.receiver_name = receiver_name;
    }

    public interface OrderListener{
        public void onOrderStatusChange(Order order);
    }
}
