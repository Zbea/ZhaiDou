package com.zhaidou.model;

import java.io.Serializable;

/**
 * Created by wangclark on 15/7/27.
 */
public class GoodInfo implements Serializable
{
    private int id;
    private String title;
    private String value;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public GoodInfo(int id, String title, String value) {
        this.id = id;
        this.title = title;
        this.value = value;
    }

    @Override
    public String toString() {
        return "GoodInfo{" +
                "title='" + title + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
