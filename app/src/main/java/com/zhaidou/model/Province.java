package com.zhaidou.model;

import java.util.List;

/**
 * Created by wangclark on 15/7/30.
 */
public class Province extends Address{
    private List<City> cityList;

    public List<City> getCityList() {
        return cityList;
    }

    public void setCityList(List<City> cityList) {
        this.cityList = cityList;
    }
}
