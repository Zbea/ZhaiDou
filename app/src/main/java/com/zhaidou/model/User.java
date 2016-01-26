package com.zhaidou.model;

import java.io.Serializable;

/**
 * Created by wangclark on 15/6/18.
 */
public class User implements Serializable{
    private int id;
    private String email;
    private String create_at;
    private String update_at;
    private String authentication_token;
    private String state;
    private String avatar;
    private String temp_token;
    private String nickName;
    private String is_featured;
    private String provider;
    private String uid;
    private int best_ranking;
    private String gender;
    private String province;
    private String city;
    private int like_number;
    private int view_number;
    private int following_number;
    private int follower_number;
    private int collocation_number;
    private boolean o_authed;
    private boolean confirmed;
    private String company;
    private String address1;
    private String address2;
    private String birthday;
    private String description;
    private String mobile;
    private boolean verified;
    private String first_name;
    private String phone;


    public User() {
    }

    public User(int id, String email, String authentication_token, String nickName, String avatar) {
        this.id = id;
        this.email = email;
        this.authentication_token = authentication_token;
        this.nickName = nickName;
        this.avatar = avatar;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCreate_at() {
        return create_at;
    }

    public void setCreate_at(String create_at) {
        this.create_at = create_at;
    }

    public String getUpdate_at() {
        return update_at;
    }

    public void setUpdate_at(String update_at) {
        this.update_at = update_at;
    }

    public String getAuthentication_token() {
        return authentication_token;
    }

    public void setAuthentication_token(String authentication_token) {
        this.authentication_token = authentication_token;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getTemp_token() {
        return temp_token;
    }

    public void setTemp_token(String temp_token) {
        this.temp_token = temp_token;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getIs_featured() {
        return is_featured;
    }

    public void setIs_featured(String is_featured) {
        this.is_featured = is_featured;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getBest_ranking() {
        return best_ranking;
    }

    public void setBest_ranking(int best_ranking) {
        this.best_ranking = best_ranking;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
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

    public int getLike_number() {
        return like_number;
    }

    public void setLike_number(int like_number) {
        this.like_number = like_number;
    }

    public int getView_number() {
        return view_number;
    }

    public void setView_number(int view_number) {
        this.view_number = view_number;
    }

    public int getFollowing_number() {
        return following_number;
    }

    public void setFollowing_number(int following_number) {
        this.following_number = following_number;
    }

    public int getFollower_number() {
        return follower_number;
    }

    public void setFollower_number(int follower_number) {
        this.follower_number = follower_number;
    }

    public int getCollocation_number() {
        return collocation_number;
    }

    public void setCollocation_number(int collocation_number) {
        this.collocation_number = collocation_number;
    }

    public boolean isO_authed() {
        return o_authed;
    }

    public void setO_authed(boolean o_authed) {
        this.o_authed = o_authed;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public User(int id, String email, String create_at, String avatar, String nickName, String gender, String province, String city, int collocation_number) {
        this.id = id;
        this.email = email;
        this.create_at = create_at;
        this.avatar = avatar;
        this.nickName = nickName;
        this.gender = gender;
        this.province = province;
        this.city = city;
        this.collocation_number = collocation_number;
    }

    public User(String avatar, String email, String nickName, boolean verified, String mobile, String description) {
        this.avatar = avatar;
        this.email = email;
        this.nickName = nickName;
        this.verified = verified;
        this.mobile = mobile;
        this.description = description;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", create_at='" + create_at + '\'' +
                ", update_at='" + update_at + '\'' +
                ", authentication_token='" + authentication_token + '\'' +
                ", state='" + state + '\'' +
                ", avatar='" + avatar + '\'' +
                ", temp_token='" + temp_token + '\'' +
                ", nickName='" + nickName + '\'' +
                ", is_featured='" + is_featured + '\'' +
                ", provider='" + provider + '\'' +
                ", uid='" + uid + '\'' +
                ", best_ranking=" + best_ranking +
                ", gender='" + gender + '\'' +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", like_number=" + like_number +
                ", view_number=" + view_number +
                ", following_number=" + following_number +
                ", follower_number=" + follower_number +
                ", collocation_number=" + collocation_number +
                ", o_authed=" + o_authed +
                ", confirmed=" + confirmed +
                '}';
    }
}
