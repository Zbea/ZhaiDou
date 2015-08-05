package com.zhaidou.model;

/**
 * Created by wangclark on 15/8/5.
 */
public class Receiver {
    private int id;
    private String address;
    private String phone;
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Receiver(int id, String address, String phone, String name) {
        this.id = id;
        this.address = address;
        this.phone = phone;
        this.name = name;
    }

    public Receiver() {
    }
}
