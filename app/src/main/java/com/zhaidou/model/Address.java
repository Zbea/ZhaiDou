package com.zhaidou.model;

/**
 * Created by wangclark on 15/7/29.
 */
public class Address {

    protected int id;
    protected String name;
    private boolean is_default;
    private String phone;
    private int user_id;
    private String address;
    private int provider_id;

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
                '}';
    }
}
