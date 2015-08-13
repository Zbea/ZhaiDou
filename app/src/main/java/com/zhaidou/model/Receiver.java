package com.zhaidou.model;

/**
 * Created by wangclark on 15/8/5.
 */
public class Receiver {
    private int id;
    private String address;
    private String province;
    private String city;
    private String area;
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

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public Receiver(int id, String address, String phone, String name) {
        this.id = id;
        this.address = address;
        this.phone = phone;
        this.name = name;
    }

    public Receiver(int id, String address, String province, String city, String area, String phone, String name) {
        this.id = id;
        this.address = address;
        this.province = province;
        this.city = city;
        this.area = area;
        this.phone = phone;
        this.name = name;
    }

    public Receiver() {
    }
}
