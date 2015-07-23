package com.zhaidou.model;

/**
 * Created by wangclark on 15/7/21.
 */
public class Logistics {
    private String date;
    private String msg;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "Logistics{" +
                "date='" + date + '\'' +
                ", msg='" + msg + '\'' +
                '}';
    }
}
