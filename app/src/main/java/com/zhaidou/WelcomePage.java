package com.zhaidou;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.ImageView;

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
                Intent intent=new Intent(WelcomePage.this,MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.main_into_the, R.anim.main_out_the);
                finish();
            }
        },  2000);
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
