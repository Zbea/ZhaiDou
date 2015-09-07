package com.zhaidou.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangclark on 15/6/12.
 */
public class SwitchImage {
    public int id;
    public int type;
    public String typeValue;
    public String title;
    public String imageUrl;


    public SwitchImage(String url, int id, String title,String imageUrl) {
        this.id = id;
        this.title = title;
        this.imageUrl=imageUrl;
    }

    public SwitchImage() {
    }

}
