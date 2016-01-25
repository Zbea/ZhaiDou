package com.zhaidou.model;

import java.util.List;

/**
 * Created by wangclark on 15/7/30.
 */
public class City extends Address {





    private List<Area> areas;

    public List<Area> getAreas() {
        return areas;
    }

    public void setAreas(List<Area> areas) {
        this.areas = areas;
    }


}
