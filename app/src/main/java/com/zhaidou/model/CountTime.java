package com.zhaidou.model;

/**
 * Created by wangclark on 15/7/2.
 */
public class CountTime {
    private long day;
    private long hour;
    private long minute;
    private long second;

    public long getDay() {
        return day;
    }

    public long getHour() {
        return hour;
    }

    public long getMinute() {
        return minute;
    }

    public long getSecond() {
        return second;
    }

    public CountTime(long day, long hour, long minute, long second) {
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
    }

    public CountTime() {
    }
}
