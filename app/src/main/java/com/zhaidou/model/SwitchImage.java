package com.zhaidou.model;

/**
 * Created by wangclark on 15/6/12.
 */
public class SwitchImage {
    public int id;
    public int type;
    public String typeValue;
    public String title;
    public String imageUrl;
    public int template_type;

    @Override
    public String toString() {
        return "SwitchImage{" +
                "id=" + id +
                ", type=" + type +
                ", typeValue='" + typeValue + '\'' +
                ", title='" + title + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", template_type=" + template_type +
                '}';
    }
}
