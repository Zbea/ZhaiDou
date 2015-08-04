package com.zhaidou.model;

/**
 * Created by wangclark on 15/7/27.
 */
public class Specification {
    private int id;
    private String title;
    public int num;
    public double price;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Specification(int id, String title,int num,double price) {
        this.id = id;
        this.title = title;
        this.num = num;
        this.price = price;

    }

    @Override
    public String toString() {
        return "Specification{" +
                "id=" + id +
                ", title='" + title + '\'' +
                '}';
    }
}
