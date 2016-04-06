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

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.R;
import com.zhaidou.ZDApplication;
import com.zhaidou.ZhaiDou;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.User;
import com.zhaidou.model.ZhaiDouRequest;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.CustomEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by roy on 15/9/16.
 */
public class AccountRegisterSetPwdActivity extends FragmentActivity {
    private CustomEditText mCodeView, mPwdView;
    private TextView headTitle;
    private TextView mRegister, mGetCode;
    private RequestQueue mRequestQueue;
    private SharedPreferences mSharedPreferences;
    private Dialog mDialog;
    private int initTime = 60;
    private Timer mTimer;
    private String phone;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    ToolUtils.setLog("33333");
                    User user = (User) msg.obj;
                    SharedPreferencesUtil.saveUser(getApplicationContext(), user);
                    Intent intent = new Intent();
                    intent.putExtra("id", user.getId());
                    intent.putExtra("email", user.getEmail());
                    intent.putExtra("token", user.getAuthentication_token());
                    intent.putExtra("nick", user.getNickName());
                    intent.putExtra("phone",user.getPhone());
                    setResult(2000, intent);
                    finish();
                    break;
            }
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
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
                    } else if (pwd.length() > 16) {
                        ToolUtils.setToast(getApplicationContext(), "抱歉,设置的密码过长");
                        mPwdView.setShakeAnimation();
                        return;
                    } else if (pwd.length() < 6) {
                        ToolUtils.setToast(getApplicationContext(), "抱歉,设置的密码过短");
                        mPwdView.setShakeAnimation();
                        return;
                    }
                    doRegister(phone,code,pwd);
                    break;
                case R.id.back_btn:
                    finish();
                    break;
                case R.id.bt_getCode:
//                    getCode();
//                    codeTimer();
                    getVerifyCode();
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
        phone=getIntent().getStringExtra("phone");

        headTitle = (TextView) findViewById(R.id.title_tv);
        headTitle.setText(R.string.title_register_set);

        mCodeView = (CustomEditText) findViewById(R.id.tv_code);
        mPwdView = (CustomEditText) findViewById(R.id.tv_psd);
        mRegister = (TextView) findViewById(R.id.bt_register);
        mGetCode = (TextView) findViewById(R.id.bt_getCode);
        mGetCode.setOnClickListener(onClickListener);

        mSharedPreferences = getSharedPreferences("zhaidou", Context.MODE_PRIVATE);

        mRequestQueue = Volley.newRequestQueue(this);
        mRegister.setOnClickListener(onClickListener);
        findViewById(R.id.back_btn).setOnClickListener(onClickListener);
    }

    /**
     * 获得验证码  &flag=1
     */
    private void getVerifyCode() {
        codeTimer();
        ZhaiDouRequest request = new ZhaiDouRequest(AccountRegisterSetPwdActivity.this,ZhaiDou.USER_REGISTER_VERIFY_CODE_URL+"?phone="+phone+"&flag=1",new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                JSONObject dataObj = jsonObject.optJSONObject("data");
                String message=dataObj.optString("message");
                int status= dataObj.optInt("status");
                if (status==201){
                    Toast.makeText(AccountRegisterSetPwdActivity.this,"获取验证码成功",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(AccountRegisterSetPwdActivity.this,message,Toast.LENGTH_SHORT).show();
                }
            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });
        ((ZDApplication)getApplication()).mRequestQueue.add(request);
    }

    /**
     * 验证码倒计时
     */
    private void codeTimer() {
        initTime = ZhaiDou.VERFIRY_TIME;
        mGetCode.setBackgroundResource(R.drawable.btn_no_click_selector);
        mGetCode.setText("重新获取(" + initTime + ")");
        mGetCode.setClickable(false);
        mTimer = new Timer();
        mTimer.schedule(new MyTimer(), 1000, 1000);
    }

    /**
     * 倒计时
     */
    class MyTimer extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    initTime = initTime - 1;
                    mGetCode.setText("重新获取(" + initTime + ")");
                    if (initTime <= 0) {
                        mTimer.cancel();
                        mGetCode.setText("获取验证码");
                        mGetCode.setBackgroundResource(R.drawable.btn_green_click_bg);
                        mGetCode.setClickable(true);
                    }
                }
            });
        }
    }


    private void doRegister(String phone, String code, String pwd) {

        mDialog = CustomLoadingDialog.setLoadingDialog(AccountRegisterSetPwdActivity.this, "注册中");
        JSONObject valueParams = new JSONObject();
        try
        {
            valueParams.put("phone", phone);
            valueParams.put("vcode", code);
            valueParams.put("password", pwd);
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ZhaiDou.USER_REGISTER_WITH_PHONE_URL, valueParams, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                if (mDialog != null)
                    mDialog.dismiss();
                ToolUtils.setLog(jsonObject.toString());
                JSONObject dataObj=jsonObject.optJSONObject("data");
                if (dataObj!=null)
                {
                    ToolUtils.setLog("11111");
                    int status = dataObj.optInt("status");
                    String msg = dataObj.optString("message");
                    if (201 != status) {
                        Toast.makeText(AccountRegisterSetPwdActivity.this, msg, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    JSONObject userObj = dataObj.optJSONObject("user");
                    int id = userObj.optInt("id");
                    String email = userObj.optString("email");
                    String token = userObj.optString("authentication_token");
                    String state = userObj.optString("state");
                    String phone = userObj.optString("phone");
                    String avatar = userObj.optJSONObject("avatar").optJSONObject("mobile_icon").optString("url");
                    String nickname = userObj.optString("nick_name");
                    User user = new User(id, email, token, nickname, avatar);
                    user.setPhone(phone);
                    ToolUtils.setLog("222222");
                    Message message = new Message();
                    message.what = 0;
                    message.obj = user;
                    handler.sendMessage(message);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError)
            {
                if (mDialog!=null)
                {
                    mDialog.dismiss();
                }
                if (volleyError!=null)
                {
                    try
                    {
                        JSONObject jsonObject=new JSONObject(new String(volleyError.networkResponse.data)) ;
                        JSONObject dataObj=jsonObject.optJSONObject("data");
                        if (dataObj!=null)
                        {
                            int status = dataObj.optInt("status");
                            String msg = dataObj.optString("message");
                            if (201 != status) {
                                Toast.makeText(AccountRegisterSetPwdActivity.this, msg, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            JSONObject userObj = dataObj.optJSONObject("user");
                            int id = userObj.optInt("id");
                            String email = userObj.optString("email");
                            String token = userObj.optString("authentication_token");
                            String state = userObj.optString("state");
                            String phone = userObj.optString("phone");
                            String avatar = userObj.optJSONObject("avatar").optJSONObject("mobile_icon").optString("url");
                            String nickname = userObj.optString("nick_name");
                            User user = new User(id, email, token, nickname, avatar);
                            user.setPhone(phone);
                            Message message = new Message();
                            message.what = 0;
                            message.obj = user;
                            handler.sendMessage(message);
                        }
                    } catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("ZhaidouVesion", getApplicationContext().getResources().getString(R.string.app_versionName));
                return headers;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(20*1000,1,1.0f));
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
    protected void onDestroy() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        super.onDestroy();
    }
}
