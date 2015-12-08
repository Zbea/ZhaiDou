package com.zhaidou.model;

import java.io.Serializable;

/**
 * Created by wangclark on 15/7/29.
 */
public class Address implements Serializable
{

    public int id;
    protected String name;
    private boolean is_default;
    private String phone;
    private int user_id;
    public String address;
    private int provider_id;
    private String updated_at;
    private String created_at;

    public String province;
    public String city;
    private String area;

    private double price;

    public int type;
    public String mobile;
    public String provinceName;
    public String cityName;


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

    public boolean isIs_default() {
        return is_default;
    }

    public void setIs_default(boolean is_default) {
        this.is_default = is_default;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getProvider_id() {
        return provider_id;
    }

    public void setProvider_id(int provider_id) {
        this.provider_id = provider_id;
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

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public double getPrice()
    {
        return price;
    }

    public void setPrice(double price)
    {
        this.price = price;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public Address() {
    }

    public Address(int id, String name, boolean is_default, String phone, int user_id, String address, int provider_id,double price) {
        this.id = id;
        this.name = name;
        this.is_default = is_default;
        this.phone = phone;
        this.user_id = user_id;
        this.address = address;
        this.provider_id = provider_id;
        this.price=price;
    }

    @Override
    public String toString() {
        return "Address{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", is_default=" + is_default +
                ", phone='" + phone + '\'' +
                ", user_id=" + user_id +
                ", address='" + address + '\'' +
                ", provider_id=" + provider_id +
                ", price=" + price +
                '}';
    }
}
