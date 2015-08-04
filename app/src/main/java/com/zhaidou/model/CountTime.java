package com.zhaidou.model;

import android.widget.TextView;

/**
 * Created by wangclark on 15/7/2.
 */
public class CountTime {
    private long day;
    private long hour;
    private long minute;
    private long second;
    private TextView mTimerView;

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

    public TextView getmTimerView() {
        return mTimerView;
    }

    public void setmTimerView(TextView mTimerView) {
        this.mTimerView = mTimerView;
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
