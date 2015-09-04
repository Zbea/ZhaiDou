package com.zhaidou.utils;

import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.zhaidou.model.CountTime;

import java.util.TimerTask;

/**
 * Created by wangclark on 15/8/4.
 */
public class TimerUtils {
    private MyTimer mTimer;
    private TextView textView;
    private long time;
    private final int UPDATE_COUNT_DOWN_TIME=1;
    private final int UPDATE_UI_TIMER_FINISH=2;
    private final int UPDATE_TIMER_START=3;
    private TimerListener timerListener;

    private Handler mHandler =new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UPDATE_TIMER_START:
                    break;
                case UPDATE_COUNT_DOWN_TIME:
                    CountTime time=(CountTime)msg.obj;
                    TextView mTimerView=time.getmTimerView();
                    mTimerView.setText(time.getMinute()+":"+time.getSecond());
                    break;
            }
        }
    };

    public void stateTimer(ListView listView,TextView mTimerView,final long temp,int position,TimerListener timerListener){
        int firstVisiblePosition=listView.getFirstVisiblePosition();
        int i = position - firstVisiblePosition;
        this.timerListener=timerListener;
        textView=mTimerView;
        time=temp;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                MyTimer mTimer=new MyTimer(textView,temp,1000);
                mTimer.start();
                Looper.loop();
            }
        }).start();

    }

    private class MyTimerTask extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... voids) {
//            Looper.prepare();
            MyTimer mTimer=new MyTimer(textView,time,1000);
            mTimer.start();
//            Looper.loop();
            return null;
        }
    }
    private class MyTimer extends CountDownTimer {
        private TextView mTimerView;
        private MyTimer(TextView mTimerView,long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            this.mTimerView=mTimerView;
        }

        @Override
        public void onTick(long l) {

            long day=24*3600*1000;
            long hour=3600*1000;
            long minute=60*1000;
            //两个日期想减得到天数
            long dayCount= l/day;
            long hourCount= (l-(dayCount*day))/hour;
            long minCount=(l-(dayCount*day)-(hour*hourCount))/minute;
            long secondCount=(l-(dayCount*day)-(hour*hourCount)-(minCount*minute))/1000;
            CountTime time = new CountTime(dayCount,hourCount,minCount,secondCount);
            Log.i("onTick------------>",l+"");
            timerListener.onTick(mTimerView,time,l);

//            mTimerView.setText(secondCount+"");
//            time.setmTimerView(mTimerView);
//            Message message =new Message();
//            message.what=UPDATE_COUNT_DOWN_TIME;
//            message.obj=time;
//            mHandler.sendMessage(message);
        }

        @Override
        public void onFinish() {
            Log.i("onFinish---------->", "onFinish");
//            mHandler.sendEmptyMessage(UPDATE_UI_TIMER_FINISH);
        }
    }

    public interface TimerListener{
        public void onTick(TextView mTimerView,CountTime countTime,long l);
    }
}
