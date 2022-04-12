package com.zhaidou.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.zhaidou.ZDApplication;
import com.zhaidou.utils.ToolUtils;

public class TimerTextView extends TextView implements Runnable
{
    // 时间变量
    private int day, hour, minute, second;
    // 当前计时器是否运行
    private boolean isRun = false;
    long totalTime;

    public TimerTextView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        initTypeFace(context);
    }

    public TimerTextView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initTypeFace(context);
    }

    public TimerTextView(Context context)
    {
        super(context);
        initTypeFace(context);
    }

    public void initTypeFace(Context context)
    {
        ZDApplication application = (ZDApplication) context.getApplicationContext();
        Typeface mTypeFace = application.getTypeFace();
        if (mTypeFace != null)
        {
            setTypeface(mTypeFace);
        }
    }

    /**
     * 将倒计时时间毫秒数转换为自身变量
     *
     * @param time 时间间隔毫秒数
     */
    public void setTimes(long time)
    {
        totalTime=time;
        //将毫秒数转化为时间
        this.second = (int) (time / 1000) % 60;
        this.minute = (int) (time / (60 * 1000) % 60);
        this.hour = (int) (time / (60 * 60 * 1000) % 24);
        this.day = (int) (time / (24 * 60 * 60 * 1000));
    }

    public long getTimes()
    {
        return totalTime;
    }

    /**
     * 显示当前时间
     *
     * @return
     */
    public String showTime()
    {
        StringBuilder time = new StringBuilder();
        time.append("剩余");
        time.append(day);
        time.append("天");
        time.append(hour);
        time.append("时");
        time.append(minute);
        time.append("分");
        time.append(second);
        time.append("秒");
        if (day <=0 & hour <= 0 & minute <= 0 & second <= 0)
        {
            totalTime=0;
            return "已结束";
        }
        return time.toString();
    }

    /**
     * 实现倒计时
     */
    private void countdown()
    {
        if (second == 0)
        {
            if (minute == 0)
            {
                if (hour == 0)
                {
                    if (day == 0)
                    {
                        //当时间归零时停止倒计时
                        isRun = false;
                        this.setText("已结束");
                        return;
                    } else
                    {
                        day--;
                    }
                    hour = 23;
                } else
                {
                    hour--;
                }
                minute = 59;
            } else
            {
                minute--;
            }
            second = 60;
        }

        second--;
    }

    public boolean isRun()
    {
        return isRun;
    }

    /**
     * 开始计时
     */
    public void start()
    {
        isRun = true;
        run();
    }

    /**
     * 结束计时
     */
    public void stop()
    {
        isRun = false;
    }

    /**
     * 实现计时循环
     */
    @Override
    public void run()
    {
        if (isRun)
        {
            totalTime=totalTime-1000;
            countdown();
            this.setText(showTime());
            postDelayed(this, 1000);
        } else
        {
            removeCallbacks(this);
        }
    }

}
