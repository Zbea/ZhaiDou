package com.zhaidou.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangclark on 15/6/12.
 */
public class SwitchImage {
    public  String url;
    public int id;
    public String title;
    public String imageUrl;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

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

    public SwitchImage(String url, int id, String title,String imageUrl) {
        this.url = url;
        this.id = id;
        this.title = title;
        this.imageUrl=imageUrl;
    }

    public SwitchImage() {
    }

    @Override
    public String toString() {
        return "SwitchImage{" +
                "url='" + url + '\'' +
                ", id=" + id +
                ", title='" + title + '\'' +
                '}';
    }
}
