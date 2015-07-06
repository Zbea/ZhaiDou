package com.zhaidou.model;

import java.util.List;

/**
 * Created by wangclark on 15/6/12.
 */
public class Product {
    private int id;
    private String title;
    private int price;
    private String url;
    private int bean_like_count;
    private List<Category> categories;
    private int remaining;

    private String end_date;

    private String image;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getBean_like_count() {
        return bean_like_count;
    }

    public void setBean_like_count(int bean_like_count) {
        this.bean_like_count = bean_like_count;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public Product(int id, String title, int price, String url, int bean_like_count, List<Category> categories, String image) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.url = url;
        this.bean_like_count = bean_like_count;
        this.categories = categories;
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getRemaining() {
        return remaining;
    }

    public void setRemaining(int remaining) {
        this.remaining = remaining;
    }

    public String getEnd_date() {
        return end_date;
    }

    public void setEnd_date(String end_date) {
        this.end_date = end_date;
    }

    public Product() {
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", price=" + price +
                ", url='" + url + '\'' +
                ", bean_like_count=" + bean_like_count +
                ", categories=" + categories +
                ", image='" + image + '\'' +
                '}';
    }
}
