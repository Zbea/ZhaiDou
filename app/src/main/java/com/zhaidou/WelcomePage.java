package com.zhaidou;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.ImageView;

import com.umeng.analytics.MobclickAgent;

import cn.jpush.android.api.JPushInterface;

import com.tencent.stat.MtaSDkException;
import com.tencent.stat.StatConfig;
import com.tencent.stat.StatCustomLogger;
import com.tencent.stat.StatGameUser;
import com.tencent.stat.StatReportStrategy;
import com.tencent.stat.StatService;
import com.tencent.stat.StatServiceImpl;
import com.tencent.stat.StatSpecifyReportedInfo;
import com.tencent.stat.common.StatConstants;
import com.tencent.stat.common.StatLogger;


/**
 * Created by roy on 15/7/22.
 */
public class WelcomePage extends Activity
{
    private static StatLogger logger = new StatLogger("MTADemon");

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.welcom_page);

//        initMTAConfig(true);
//        String appkey = "Aqc1104776552";
//        // 初始化并启动MTA
//        // 第三方SDK必须按以下代码初始化MTA,其中appkey为规定的格式或MTA分配的代码。 // 其它普通的app可自行选择是否调用
//        try
//        {
//        // 第三个参数必须为:com.tencent.stat.common.StatConstants.VERSION
//        StatService.startStatService(this, appkey,com.tencent.stat.common.StatConstants.VERSION);
//        } catch (MtaSDkException e)
//        {
//        // MTA初始化失败 logger.error("MTA start failed."); logger.error("e");
//        }

        registerMessageReceiver();
        initView();
//        TestinAgent.init(this, "a6fa2001f0268f633bd007e5b0f118ca");
//        TestinAgent.setLocalDebug(true);
    }

    private void initMTAConfig(boolean isDebugMode)
    {
        if (isDebugMode)
        {
            StatConfig.setDebugEnable(true);
            StatConfig.setMTAPreferencesFileName("test");
            StatConfig.setStatSendStrategy(StatReportStrategy.INSTANT);
            StatConfig.setSessionTimoutMillis(5 * 60 * 1000);
        } else
        {
            StatConfig.setDebugEnable(false);
            StatConfig.setAutoExceptionCaught(true);
            StatConfig.setStatSendStrategy(StatReportStrategy.APP_LAUNCH);
        }
    }

    /**
     * 初始化操作
     */
    private void initView()
    {
        ImageView image = (ImageView) findViewById(R.id.welcomeBtn);

        new android.os.Handler().postDelayed(new Runnable()
        {
            public void run()
            {
                if (isFirstEnter())
                {
                    Intent intent = new Intent(WelcomePage.this, WelcomeGuidancePage.class);
                    startActivity(intent);
                } else
                {
                    Intent intent = new Intent(WelcomePage.this, MainActivity.class);
                    startActivity(intent);
                }
                overridePendingTransition(R.anim.enter_into_the, R.anim.enter_out_the);
                finish();
            }
        }, 2000);
    }

    public boolean isFirstEnter()
    {
        SharedPreferences preferences = getSharedPreferences("phone", Context.MODE_PRIVATE);
        if (preferences.getBoolean("firststart", true))
        {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("firststart", false);
            editor.commit();
            return true;
        } else
        {
            return false;
        }
    }

    /**
     * 让返回键失效
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_BACK:
                return true;
            case KeyEvent.KEYCODE_HOME:
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //for receive customer msg from jpush server
    private MessageReceiver mMessageReceiver;
    public static final String MESSAGE_RECEIVED_ACTION = "com.zhaidou.jpush.MESSAGE_RECEIVED_ACTION";
    public static final String KEY_TITLE = "title";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_EXTRAS = "extras";

    public void registerMessageReceiver()
    {
        mMessageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        filter.addAction(MESSAGE_RECEIVED_ACTION);
        registerReceiver(mMessageReceiver, filter);
    }

    public class MessageReceiver extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (MESSAGE_RECEIVED_ACTION.equals(intent.getAction()))
            {
                String message = intent.getStringExtra(KEY_MESSAGE);
                String extras = intent.getStringExtra(KEY_EXTRAS);
                StringBuilder showMsg = new StringBuilder();
                showMsg.append(KEY_MESSAGE + " : " + message + "\n");
                if (!TextUtils.isEmpty(extras))
                {
                    showMsg.append(KEY_EXTRAS + " : " + extras + "\n");
                }
            }
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(mMessageReceiver);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        JPushInterface.onResume(this);
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        JPushInterface.onPause(this);
        MobclickAgent.onPause(this);
    }
}
