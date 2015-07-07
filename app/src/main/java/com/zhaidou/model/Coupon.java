package com.zhaidou.model;

/**
 * Created by wangclark on 15/7/2.
 */
public class Coupon {
    private int id;
    private String created_at;
    private String updated_at;
    private String for_date;
    private String url;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getFor_date() {
        return for_date;
    }

    public void setFor_date(String for_date) {
        this.for_date = for_date;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Coupon(int id, String created_at, String updated_at, String for_date, String url) {
        this.id = id;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.for_date = for_date;
        this.url = url;
    }

    public Coupon() {
    }

    @Override
    public String toString() {
        return "Coupon{" +
                "id=" + id +
                ", created_at='" + created_at + '\'' +
                ", updated_at='" + updated_at + '\'' +
                ", for_date='" + for_date + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
