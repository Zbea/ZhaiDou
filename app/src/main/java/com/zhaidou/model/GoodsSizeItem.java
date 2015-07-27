package com.zhaidou.model;

import java.io.Serializable;

/**
 * Created by roy on 15/7/24.
 */
public class GoodsSizeItem implements Serializable
{
    //id
    public int id;
    //名称
    public String title;
    //是否买完
    public boolean isNull;
    //是否选择
    public boolean isSeclect;

    @Override
    public String toString()
    {
        return "GoodsSizeItem{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", isNull=" + isNull +
                ", isSeclect=" + isSeclect +
                '}';
    }

    public GoodsSizeItem(int id, String title, boolean isNull, boolean isSeclect)
    {
        this.id = id;
        this.title = title;
        this.isNull = isNull;
        this.isSeclect = isSeclect;
    }
}
