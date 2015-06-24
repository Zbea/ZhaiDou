package com.zhaidou.model;

import java.util.List;

/**
 * Created by wangclark on 15/6/12.
 */
public class Category {
    private int id;
    private String name;
    private String avatar;
    private String thumb;

    private List<CategoryItem> categoryItems;

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

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public Category(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Category() {
    }

    public List<CategoryItem> getCategoryItems() {
        return categoryItems;
    }

    public void setCategoryItems(List<CategoryItem> categoryItems) {
        this.categoryItems = categoryItems;
    }

    public Category(int id, String name, String avatar, String thumb, List<CategoryItem> categoryItems) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
        this.thumb = thumb;
        this.categoryItems = categoryItems;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", avatar='" + avatar + '\'' +
                ", thumb='" + thumb + '\'' +
                ", categoryItems=" + categoryItems +
                '}';
    }
}
