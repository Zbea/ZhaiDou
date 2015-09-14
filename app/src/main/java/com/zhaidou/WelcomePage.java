package com.zhaidou;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.ImageView;

import com.testin.agent.TestinAgent;
import com.umeng.analytics.AnalyticsConfig;

/**
 * Created by roy on 15/7/22.
 */
public class WelcomePage extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.welcom_page);

        initView();
//        TestinAgent.init(this, "a6fa2001f0268f633bd007e5b0f118ca");
//        TestinAgent.setLocalDebug(true);
    }

    /**
     * 初始化操作
     */
    private void initView()
    {
        ImageView image= (ImageView) findViewById(R.id.welcomeBtn);

        new android.os.Handler().postDelayed(new Runnable()
        {
            public void run()
            {
                if (isFirstEnter())
                {
                    Intent intent=new Intent(WelcomePage.this,WelcomeGuidancePage.class);
                    startActivity(intent);
                }
                else
                {
                    Intent intent=new Intent(WelcomePage.this,MainActivity.class);
                    startActivity(intent);
                }
                overridePendingTransition(R.anim.enter_into_the, R.anim.enter_out_the);
                finish();
            }
        },  2000);
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
        }
        else
        {
            return false;
        }
    }

    /**
     * 让返回键失效
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


}
