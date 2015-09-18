package com.zhaidou.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.User;
import com.zhaidou.model.ZhaiDouRequest;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.view.CustomEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by roy on 15/9/16.
 */
public class RegisterSetPwdActivity extends FragmentActivity {
    private CustomEditText mCodeView,mPwdView;
    private TextView headTitle;
    private TextView mRegister,mGetCode;
    private RequestQueue mRequestQueue;
    private SharedPreferences mSharedPreferences;
    private Dialog mDialog;
    private int initTime=60;
    private Timer mTimer;

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    User user =(User)msg.obj;
                    SharedPreferencesUtil.saveUser(getApplicationContext(), user);
                    Intent intent=new Intent();
                    intent.putExtra("id",user.getId());
                    intent.putExtra("email",user.getEmail());
                    intent.putExtra("token",user.getAuthentication_token());
                    intent.putExtra("nick",user.getNickName());
                    setResult(2000, intent);
                    finish();
                    break;
            }
        }
    };

    private View.OnClickListener onClickListener=new View.OnClickListener()
    {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.bt_register:
                    String code = mCodeView.getText().toString();
                    String pwd = mPwdView.getText().toString();
                    if (TextUtils.isEmpty(code)) {
                        mCodeView.setShakeAnimation();
                        return;
                    }
                    if (TextUtils.isEmpty(pwd)) {
                        mPwdView.setShakeAnimation();
                        return;
                    }
                    doRegister();
                    break;
                case R.id.back_btn:
                    finish();
                    break;
                case R.id.bt_getCode:
                    getCode();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.act_register_set_pwd_page);

        headTitle=(TextView)findViewById(R.id.title_tv);
        headTitle.setText(R.string.title_register_set);

        mCodeView=(CustomEditText)findViewById(R.id.tv_code);
        mPwdView=(CustomEditText)findViewById(R.id.tv_psd);
        mRegister=(TextView)findViewById(R.id.bt_register);
        mGetCode=(TextView)findViewById(R.id.bt_getCode);
        mGetCode.setOnClickListener(onClickListener);

        mSharedPreferences=getSharedPreferences("zhaidou", Context.MODE_PRIVATE);

        mRequestQueue= Volley.newRequestQueue(this);
        mRegister.setOnClickListener(onClickListener);
        findViewById(R.id.back_btn).setOnClickListener(onClickListener);

        codeTimer();
    }

    /**
     * 获得验证码
     */
    private void getCode()
    {
        codeTimer();
    }

    /**
     * 验证码倒计时
     */
    private void codeTimer()
    {
        initTime=60;
        mGetCode.setBackgroundResource(R.drawable.btn_no_click_selector);
        mGetCode.setText("重新获取("+initTime+")");
        mGetCode.setClickable(false);
        mTimer=new Timer();
        mTimer.schedule(new MyTimer(), 1000, 1000);
    }

    /**
     * 倒计时
     */
    class MyTimer extends TimerTask
    {
        @Override
        public void run()
        {
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    initTime = initTime - 1;
                    mGetCode.setText("重新获取("+initTime+")");
                    if (initTime <= 0)
                    {
                        mTimer.cancel();
                        mGetCode.setText("获取验证码");
                        mGetCode.setBackgroundResource(R.drawable.btn_green_click_bg);
                        mGetCode.setClickable(true);
                    }
                }
            });
        }
    }


    private void doRegister(){

        mDialog= CustomLoadingDialog.setLoadingDialog(RegisterSetPwdActivity.this, "注册中");
        String code = mCodeView.getText().toString();
        Map<String, String> valueParams = new HashMap<String,String>();
        valueParams.put("user[email]", code);
        ZhaiDouRequest request=new ZhaiDouRequest(Request.Method.POST,ZhaiDou.USER_REGISTER_URL,valueParams,new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                if (mDialog!=null)
                    mDialog.dismiss();
                Object obj = jsonObject.opt("message");
                if (obj!=null){
                    JSONArray errMsg =  jsonObject.optJSONArray("message");
                    Toast.makeText(RegisterSetPwdActivity.this,errMsg.optString(0),Toast.LENGTH_LONG).show();
                    return;
                }

                JSONObject userObj = jsonObject.optJSONObject("user");
                int id = userObj.optInt("id");
                String email = userObj.optString("email");
                String token = userObj.optString("authentication_token");
                String state =userObj.optString("state");
                String avatar = userObj.optJSONObject("avatar").optJSONObject("mobile_icon").optString("url");
                String nickname=userObj.optString("nick_name");
                User user=new User(id,email,token,nickname,avatar);
                Message message=new Message();
                message.what=0;
                message.obj=user;
                handler.sendMessage(message);
            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });
        mRequestQueue.add(request);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(getResources().getString(R.string.title_register_set));
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(getResources().getString(R.string.title_register_set));
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy()
    {
        if (mTimer!=null)
        {
            mTimer.cancel();
            mTimer=null;
        }
        super.onDestroy();
    }
}
