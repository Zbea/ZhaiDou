package com.zhaidou.model;/**
 * Created by wangclark on 15/11/19.
 */

/**
 * User: Scoield(553899626@qq.com)
 * Date: 2015-11-19
 * Time: 10:20
 * Description:专题实体类
 * FIXME
 */
public class SpecialItem {
    public int id;
    public String banner;
    public int topic_tag;
    public int template_type;
    public String header_img;
    public String title;

    @Override
    public String toString() {
        return "SpecialItem{" +
                "id=" + id +
                ", banner='" + banner + '\'' +
                ", topic_tag=" + topic_tag +
                ", template_type=" + template_type +
                ", header_img='" + header_img + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}